<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Post;
use App\Services\AnthropicService;

class PostSummaryController extends Controller
{
    public function __invoke(Post $post, AnthropicService $anthropic)
    {
        $comments = $post->comments()->with('user')->get();
        $commentText = $comments->count() > 0
            ? $comments->map(fn($c) => "{$c->user->name}: {$c->body}")->join("\n")
            : 'No comments';

        $prompt = "Summarize this blog post and its comments in 2-3 sentences:\n\n"
            . "Post: {$post->title}\n{$post->body}\n\n"
            . "Comments:\n{$commentText}";

        $summary = $anthropic->summarize($prompt);

        return response()->json([
            'post_id' => $post->id,
            'title' => $post->title,
            'summary' => $summary,
            'comments_count' => $comments->count(),
        ]);
    }
}
