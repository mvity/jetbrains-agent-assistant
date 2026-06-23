import { FormEvent, useCallback, useMemo, useState } from 'react';
import { sendBridgeEvent } from './bridge';
import { useAgentEvents } from './useAgentEvents';
import type { AgentEvent, ChatMessage } from './types';
import './styles.css';

const initialMessages: ChatMessage[] = [
  {
    id: 'system-ready',
    role: 'system',
    text: 'Agent runtime shell is ready.',
  },
];

const runtimes = [
  { id: 'fake-agent', label: 'Fake' },
  { id: 'claude', label: 'Claude' },
  { id: 'codex', label: 'Codex' },
  { id: 'opencode', label: 'OpenCode' },
];

function createMessage(role: ChatMessage['role'], text: string): ChatMessage {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    text,
  };
}

export default function App() {
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const [input, setInput] = useState('');
  const [runtimeId, setRuntimeId] = useState('fake-agent');
  const [bridgeAvailable, setBridgeAvailable] = useState(() => Boolean(window.sendToJava));
  const [sessionLabel, setSessionLabel] = useState('No session yet');

  const handleAgentEvent = useCallback((event: AgentEvent) => {
    if (event.type === 'agent.session.created') {
      setSessionLabel(event.providerSessionId ? `Session ${event.providerSessionId.slice(0, 8)}` : 'Session started');
      return;
    }
    if (event.type === 'agent.message.delta') {
      setMessages((current) => [...current, createMessage('assistant', event.text ?? '')]);
      return;
    }
    if (event.type === 'agent.error') {
      setMessages((current) => [...current, createMessage('error', event.message ?? 'Unknown error')]);
    }
  }, []);

  useAgentEvents(handleAgentEvent);

  const statusText = useMemo(() => {
    return bridgeAvailable ? `${sessionLabel} · ${runtimeId}` : 'Preview mode · bridge unavailable';
  }, [bridgeAvailable, runtimeId, sessionLabel]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const text = input.trim();
    if (!text) {
      return;
    }

    setMessages((current) => [...current, createMessage('user', text)]);
    setInput('');

    const sent = sendBridgeEvent({ type: 'sendMessage', runtimeId, text });
    setBridgeAvailable(sent);
    if (!sent) {
      setMessages((current) => [
        ...current,
        createMessage('assistant', 'Bridge is not available in this preview.'),
      ]);
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <h1>Agent Assistant</h1>
          <p>{statusText}</p>
        </div>
        <label className="runtime-picker">
          <span>Runtime</span>
          <select
            value={runtimeId}
            onChange={(event) => {
              setRuntimeId(event.target.value);
              setSessionLabel('No session yet');
            }}
          >
            {runtimes.map((runtime) => (
              <option key={runtime.id} value={runtime.id}>
                {runtime.label}
              </option>
            ))}
          </select>
        </label>
      </header>

      <main className="messages" aria-label="Conversation">
        {messages.map((message) => (
          <article className={`message message-${message.role}`} key={message.id}>
            <div className="message-role">{message.role}</div>
            <div className="message-text">{message.text}</div>
          </article>
        ))}
      </main>

      <form className="composer" onSubmit={handleSubmit}>
        <textarea
          aria-label="Message"
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask the fake agent something..."
        />
        <button type="submit">Send</button>
      </form>
    </div>
  );
}
