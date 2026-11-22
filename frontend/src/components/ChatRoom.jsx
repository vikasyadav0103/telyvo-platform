import { useEffect, useRef, useState } from 'react';
import { BaseChannel, ConnectionStatus, ConnectionStatusHandler, DirectChannel, GroupChannel, GroupChannelIdentifier, LogLevel, MessageHandler, NCEngine, SendTextMessageParams } from '@nexconn/chat';
import SendRoundedIcon from '@mui/icons-material/SendRounded';

export default function ChatRoom() {
  const [config, setConfig] = useState(null);
  const [status, setStatus] = useState('Loading chat configuration');
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const channelRef = useRef(null);
  const configRef = useRef(null);
  const seenRef = useRef(new Set());
  const retryTimerRef = useRef(null);
  const syncTimerRef = useRef(null);
  const participant = new URLSearchParams(window.location.search).get('participant') === 'peer' ? 'peer' : 'primary';

  useEffect(() => {
    let active = true;
    const controller = new AbortController();
    const timeout = window.setTimeout(() => controller.abort(), 10000);
    const messageHandlerId = `telyvo-message-handler-${participant}`;
    const connectionHandlerId = `telyvo-connection-handler-${participant}`;
    const describeError = (error) => {
      if (error?.name === 'AbortError') return 'Chat configuration timed out. Check that the backend is running.';
      return error?.message || 'Unable to connect to Nexconn.';
    };
    const parseResponse = async (response) => {
      const body = await response.text();
      let data = {};
      try { data = body ? JSON.parse(body) : {}; } catch { throw new Error('Backend returned an invalid chat configuration response.'); }
      if (!response.ok) throw new Error(data.message || `Chat setup failed (HTTP ${response.status}).`);
      if (!data.appKey || !data.token || !data.userId || (!data.groupId && !data.targetUserId)) {
        throw new Error('Chat configuration is incomplete. Check Nexconn App Key, user and peer/group settings.');
      }
      return data;
    };
    async function connect() {
      try {
        const response = await fetch('/api/v1/chat/config?participant=' + encodeURIComponent(participant), { signal: controller.signal });
        const data = await parseResponse(response);
        if (!active) return;
        setConfig(data);
        configRef.current = data;
        NCEngine.initialize({ appKey: data.appKey, logLevel: LogLevel.ERROR });
        NCEngine.addConnectionStatusHandler(connectionHandlerId, new ConnectionStatusHandler({
          onConnectionStatusChanged({ status: connectionStatus, code }) {
            if (!active) return;
            const isConnected = connectionStatus === ConnectionStatus.CONNECTED;
            setConnected(isConnected);
            if (isConnected) {
              const channelDesc = data.channelType === 'direct' ? ` (Direct with ${data.targetUserId})` : (data.groupId ? ` (Group ${data.groupId})` : '');
              setStatus(`Connected as ${data.userId}${channelDesc}`);
            }
            else if (connectionStatus === ConnectionStatus.CONNECTING) setStatus('Connecting to Nexconn…');
            else if (connectionStatus === ConnectionStatus.DISCONNECTED || connectionStatus === ConnectionStatus.SUSPENDED) {
              channelRef.current = null;
              if (code === 6) {
                setStatus('Chat disconnected (code 6): Another tab connected. Open peer session in an Incognito / Private window.');
              } else {
                setStatus(`Chat disconnected${code == null ? '' : ` (code ${code})`}. Reconnecting…`);
                window.clearTimeout(retryTimerRef.current);
                retryTimerRef.current = window.setTimeout(async () => {
                  if (!active || !configRef.current?.token) return;
                  setStatus('Reconnecting to Nexconn…');
                  try {
                    const reconnectResult = await NCEngine.connect({ token: configRef.current.token });
                    if (!reconnectResult?.isOk) throw new Error(`Reconnect failed (code ${reconnectResult?.code ?? 'unknown'}).`);
                  } catch (error) {
                    if (active) setStatus(error?.message || 'Reconnect failed. Reload after waiting for the token limit to reset.');
                  }
                }, 4000);
              }
            }
          }
        }));
        NCEngine.addMessageHandler(messageHandlerId, new MessageHandler({
          onMessageReceived({ messages: incoming = [] }) {
            if (!active) return;
            setMessages((current) => {
              const next = [...current];
            if (!Array.isArray(incoming)) return;
            incoming.forEach((message) => {
                const id = message.messageId || `${message.senderUserId}-${message.sentTime}`;
                if (seenRef.current.has(id)) return;
                seenRef.current.add(id);
                const content = message.content?.text || message.content?.content || message.content || '';
                next.push({ id, sender: message.senderUserId || 'Team member', text: typeof content === 'string' ? content : JSON.stringify(content), sentTime: message.sentTime || Date.now() });
              });
              return next.slice(-100);
            });
          }
        }));
        const result = await NCEngine.connect({ token: data.token });
        if (!active) return;
        if (!result?.isOk) throw new Error(`Nexconn connection failed (code ${result?.code ?? 'unknown'}).`);
        if (data.channelType === 'direct' || (!data.groupId && data.targetUserId)) {
          channelRef.current = new DirectChannel(data.targetUserId);
          setStatus(`Connected as ${data.userId} (Direct with ${data.targetUserId})`);
        } else {
          channelRef.current = new GroupChannel(data.groupId);
          setStatus(`Connected as ${data.userId} (Group ${data.groupId})`);
          const syncMessages = async () => {
            try {
              const query = BaseChannel.createMessagesQuery({
                channelIdentifier: new GroupChannelIdentifier(data.groupId),
                isAscending: true,
                pageSize: 50,
                startTime: Date.now() - 24 * 60 * 60 * 1000
              });
              const page = await query.loadNextPage();
              const history = page?.isOk ? (page.data?.data || []) : [];
              if (!active || !Array.isArray(history)) return;
              setMessages((current) => {
                const next = [...current];
                history.forEach((message) => {
                  const id = message.messageId || `${message.senderUserId}-${message.sentTime}`;
                  if (seenRef.current.has(id)) return;
                  seenRef.current.add(id);
                  const content = message.content?.text || message.content?.content || message.content || '';
                  next.push({ id, sender: message.senderUserId || 'Team member', text: typeof content === 'string' ? content : JSON.stringify(content), sentTime: message.sentTime || Date.now() });
                });
                return next.sort((a, b) => a.sentTime - b.sentTime).slice(-100);
              });
            } catch { /* realtime delivery remains primary; history sync is best effort */ }
          };
          await syncMessages();
          window.clearInterval(syncTimerRef.current);
          syncTimerRef.current = window.setInterval(syncMessages, 3000);
        }
        setConnected(true);
      } catch (error) {
        if (active) {
          channelRef.current = null;
          setConnected(false);
          setStatus(describeError(error));
        }
      }
    }
    connect();
    return () => {
      active = false;
      controller.abort();
      window.clearTimeout(timeout);
      window.clearTimeout(retryTimerRef.current);
      window.clearInterval(syncTimerRef.current);
      NCEngine.disconnect().catch(() => {});
      configRef.current = null;
      channelRef.current = null;
      setConnected(false);
    };
  }, [participant]);

  async function sendMessage(event) {
    event.preventDefault();
    const value = text.trim();
    if (!value || !connected || !channelRef.current || sending) return;
    setSending(true);
    try {
      const result = await channelRef.current.sendMessage(new SendTextMessageParams({ text: value }));
      if (!result?.isOk) throw new Error(`Message send failed (code ${result?.code ?? 'unknown'}). Please retry.`);
      const message = result.data;
      const id = message?.messageId || `local-${Date.now()}`;
      if (!seenRef.current.has(id)) {
        seenRef.current.add(id);
        setMessages((current) => [...current, { id, sender: config?.userId || 'You', text: value, sentTime: message?.sentTime || Date.now() }].slice(-100));
      }
      setText('');
    } catch (error) {
      setStatus(error?.message || 'Unable to send message. Please retry.');
    } finally { setSending(false); }
  }

  return (
    <section className="chat-section" id="discuss">
      <div className="section-heading"><div><span className="section-kicker">TEAM COLLABORATION · NEXCONN</span><h2>Discuss the evidence.</h2></div><div className="chat-heading-actions"><span className="section-note">{status}</span><a href={participant === 'peer' ? '?participant=primary' : '?participant=peer'} target="_blank" rel="noreferrer" title="Open in an Incognito/Private window so both users can stay connected simultaneously">Open {participant === 'peer' ? 'primary' : 'peer'} session (Private Window) ↗</a></div></div>
      <div className="chat-panel">
        {!connected && status && status !== 'Loading chat configuration' && (
          <p className="error-message" role="alert" style={{ marginBottom: '12px' }}>{status}</p>
        )}
        <div className="chat-feed" aria-live="polite">
          {messages.length === 0 && <p className="chat-empty">No messages yet. Share the decision context with your team.</p>}
          {messages.map((message) => <article className="chat-message" key={message.id}><span>{message.sender}</span><p>{message.text}</p><time>{new Date(message.sentTime).toLocaleTimeString()}</time></article>)}
        </div>
        <form className="chat-compose" onSubmit={sendMessage}>
          <input value={text} onChange={(event) => setText(event.target.value)} maxLength={1000} placeholder="Discuss a finding or ask your team…" aria-label="Team message" disabled={!connected} />
          <button type="submit" disabled={!text.trim() || sending || !connected}><SendRoundedIcon /> Send</button>
        </form>
        <p className="chat-hint">Messages are delivered through the Nexconn Chat SDK group channel. <strong>Important:</strong> Open the peer session in an <strong>Incognito / Private window</strong> (or separate browser profile) so both users have independent WebSockets. Also ensure both users are members of the group in the Nexconn console.</p>
      </div>
    </section>
  );
}
