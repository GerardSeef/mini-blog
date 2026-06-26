<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Post;
use Illuminate\Database\QueryException;

class LikeController extends Controller
{
    public function store(Post $post)
    {
        try {
            $post->likes()->create(['user_id' => auth()->id()]);
            return response()->json(['message' => 'Like added successfully'], 201);
        } catch (QueryException $e) {
            if (str_contains($e->getMessage(), 'Duplicate') || str_contains($e->getMessage(), 'unique')) {
                return response()->json(['message' => 'Already liked'], 409);
            }
            throw $e;
        }
    }

    public function destroy(Post $post)
    {
        $post->likes()->where('user_id', auth()->id())->delete();
        return response()->json(['message' => 'Like removed successfully']);
    }
}
