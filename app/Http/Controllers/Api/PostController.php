<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\StorePostRequest;
use App\Http\Resources\PostResource;
use App\Models\Post;
use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Http\Request;

class PostController extends Controller
{
    use AuthorizesRequests;

    public function index()
    {
        $posts = Post::with('user')
            ->withCount(['comments', 'likes'])
            ->paginate(15);

        return PostResource::collection($posts);
    }

    public function store(StorePostRequest $request)
    {
        $post = auth()->user()->posts()->create($request->validated());
        $post->loadCount(['comments', 'likes']);

        return new PostResource($post);
    }

    public function show(Post $post)
    {
        $post->loadCount(['comments', 'likes']);
        return new PostResource($post);
    }

    public function update(StorePostRequest $request, Post $post)
    {
        $this->authorize('update', $post);
        $post->update($request->validated());
        $post->loadCount(['comments', 'likes']);

        return new PostResource($post);
    }

    public function destroy(Post $post)
    {
        $this->authorize('delete', $post);
        $post->delete();

        return response()->json(['message' => 'Post deleted successfully']);
    }
}
