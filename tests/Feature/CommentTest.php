<?php

namespace Tests\Feature;

use App\Models\Post;
use App\Models\User;
use Tests\TestCase;
use Illuminate\Foundation\Testing\RefreshDatabase;

class CommentTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_create_comment()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create();
        $token = auth('api')->attempt(['email' => $user->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->postJson("/api/posts/{$post->id}/comments", [
                'body' => 'Great post!',
            ]);

        $response->assertStatus(201);
        $this->assertDatabaseHas('comments', ['body' => 'Great post!']);
    }

    public function test_user_can_list_comments()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create();
        $post->comments()->create(['user_id' => $user->id, 'body' => 'Test comment']);

        $response = $this->getJson("/api/posts/{$post->id}/comments");

        $response->assertStatus(200);
        $response->assertJsonCount(1, 'data');
    }
}
