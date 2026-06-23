import { useEffect } from 'react';
import type { AgentEvent } from './types';

export function useAgentEvents(onEvent: (event: AgentEvent) => void) {
  useEffect(() => {
    window.agentAssistantReceiveEvent = onEvent;
    return () => {
      if (window.agentAssistantReceiveEvent === onEvent) {
        window.agentAssistantReceiveEvent = undefined;
      }
    };
  }, [onEvent]);
}

