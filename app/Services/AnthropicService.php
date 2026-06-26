<?php

namespace App\Services;

use GuzzleHttp\Client;

class AnthropicService
{
    private $client;
    private $apiKey;
    private $apiUrl = 'https://api.anthropic.com/v1/messages';

    public function __construct()
    {
        $this->apiKey = config('services.anthropic.api_key');
        $this->client = new Client();
    }

    public function summarize(string $prompt): string
    {
        $response = $this->client->post($this->apiUrl, [
            'headers' => [
                'Content-Type' => 'application/json',
                'x-api-key' => $this->apiKey,
                'anthropic-version' => '2023-06-01',
            ],
            'json' => [
                'model' => 'claude-haiku-4-5-20251001',
                'max_tokens' => 300,
                'messages' => [
                    [
                        'role' => 'user',
                        'content' => $prompt,
                    ],
                ],
            ],
        ]);

        $data = json_decode((string) $response->getBody(), true);
        return $data['content'][0]['text'] ?? 'Unable to generate summary';
    }
}
