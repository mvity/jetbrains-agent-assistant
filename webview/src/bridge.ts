import type { BridgePayload } from './types';

export function sendBridgeEvent(payload: BridgePayload): boolean {
  if (!window.sendToJava) {
    return false;
  }
  window.sendToJava(JSON.stringify(payload));
  return true;
}

