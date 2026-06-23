export type AgentRole = 'system' | 'user' | 'assistant' | 'error';

export type ChatMessage = {
  id: string;
  role: AgentRole;
  text: string;
};

export type AgentEvent = {
  type: string;
  runtimeId?: string | null;
  providerSessionId?: string | null;
  text?: string | null;
  message?: string | null;
  createdAt?: string | null;
};

export type BridgePayload =
  | {
      type: 'sendMessage';
      runtimeId: string;
      text: string;
    };

declare global {
  interface Window {
    sendToJava?: (message: string) => void;
    agentAssistantReceiveEvent?: (event: AgentEvent) => void;
  }
}
