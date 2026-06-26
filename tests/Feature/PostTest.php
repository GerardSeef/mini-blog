<?php

namespace Tests\Feature;

use App\Models\Post;
use App\Models\User;
use Tests\TestCase;
use Illuminate\Foundation\Testing\RefreshDatabase;

class PostTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_create_post()
    {
        $user = User::factory()->create();
        $token = auth('api')->attempt(['email' => $user->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->postJson('/api/posts', [
                'title' => 'Test Post',
                'body' => 'This is a test post',
            ]);

        $response->assertStatus(201);
        $this->assertDatabaseHas('posts', ['title' => 'Test Post']);
    }

    public function test_user_cannot_edit_other_users_post()
    {
        $owner = User::factory()->create();
        $otherUser = User::factory()->create();
        $post = Post::factory()->create(['user_id' => $owner->id]);

        $token = auth('api')->attempt(['email' => $otherUser->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->putJson("/api/posts/{$post->id}", [
                'title' => 'Updated Title',
                'body' => 'Updated body',
            ]);

        $response->assertStatus(403);
    }

    public function test_post_list_includes_counts()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create(['user_id' => $user->id]);
        $post->comments()->create(['user_id' => $user->id, 'body' => 'Test comment']);
        $post->likes()->create(['user_id' => $user->id]);

        $response = $this->getJson('/api/posts');

        $response->assertStatus(200);
        $response->assertJsonPath('data.0.comments_count', 1);
        $response->assertJsonPath('data.0.likes_count', 1);
    }
}
