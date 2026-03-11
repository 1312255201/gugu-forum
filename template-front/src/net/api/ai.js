import {post} from "@/net";

export const apiChatWithAI = (context, success, failure) =>
    post('/api/ai/chat', context, success, failure)
