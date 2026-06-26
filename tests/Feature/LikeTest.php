<?php

namespace Tests\Feature;

use App\Models\Post;
use App\Models\User;
use Tests\TestCase;
use Illuminate\Foundation\Testing\RefreshDatabase;

class LikeTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_like_post()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create();
        $token = auth('api')->attempt(['email' => $user->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->postJson("/api/posts/{$post->id}/like");

        $response->assertStatus(201);
        $this->assertDatabaseHas('likes', ['user_id' => $user->id, 'post_id' => $post->id]);
    }

    public function test_user_cannot_like_twice()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create();
        $post->likes()->create(['user_id' => $user->id]);

        $token = auth('api')->attempt(['email' => $user->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->postJson("/api/posts/{$post->id}/like");

        $response->assertStatus(409);
        $response->assertJsonPath('message', 'Already liked');
    }

    public function test_user_can_unlike_post()
    {
        $user = User::factory()->create();
        $post = Post::factory()->create();
        $post->likes()->create(['user_id' => $user->id]);

        $token = auth('api')->attempt(['email' => $user->email, 'password' => 'password']);

        $response = $this->withHeader('Authorization', "Bearer $token")
            ->deleteJson("/api/posts/{$post->id}/like");

        $response->assertStatus(200);
        $this->assertDatabaseMissing('likes', ['user_id' => $user->id, 'post_id' => $post->id]);
    }
}
