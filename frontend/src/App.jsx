import { useEffect, useState } from 'react';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import AutoAwesomeRoundedIcon from '@mui/icons-material/AutoAwesomeRounded';
import ForumRoundedIcon from '@mui/icons-material/ForumRounded';
import FactCheckRoundedIcon from '@mui/icons-material/FactCheckRounded';
import TravelExploreRoundedIcon from '@mui/icons-material/TravelExploreRounded';
import TerminalRoundedIcon from '@mui/icons-material/TerminalRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import HubRoundedIcon from '@mui/icons-material/HubRounded';
import ChatRoom from './components/ChatRoom';
import DecisionLog from './components/DecisionLog';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeSanitize from 'rehype-sanitize';

function linkResearchCitations(markdown = '') {
  return markdown.replace(/\[\[([\d\s,]+)\]\]/g, (_, numbers) => numbers.split(',').map((number) => number.trim()).filter(Boolean).map((number) => `[${number}](#research-source-${number})`).join(', '));
}

const stages = [
  { number: '01', title: 'Discover', description: 'Find current engineering resources.', icon: <TravelExploreRoundedIcon />, tag: 'SERPAPI' },
  { number: '02', title: 'Research', description: 'Compare options with cited evidence.', icon: <AutoAwesomeRoundedIcon />, tag: 'YOU.COM' },
  { number: '03', title: 'Decide', description: 'Record the outcome, status and rationale.', icon: <FactCheckRoundedIcon />, tag: 'DECISION LOG' },
  { number: '04', title: 'Discuss', description: 'Bring the team into the decision.', icon: <ForumRoundedIcon />, tag: 'NEXCONN' }
];

export default function App() {
  const [query, setQuery] = useState('');
  const [apiStatus, setApiStatus] = useState('Checking API');
  const [providers, setProviders] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [research, setResearch] = useState(null);
  const [researching, setResearching] = useState(false);
  const [researchError, setResearchError] = useState('');

  useEffect(() => {
    fetch('/api/v1/health')
      .then((response) => { if (!response.ok) throw new Error(); return response.json(); })
      .then((data) => setApiStatus(data.status === 'UP' ? 'API online' : 'API unavailable'))
      .catch(() => setApiStatus('API offline'));
    fetch('/api/v1/integrations/status')
      .then((response) => { if (!response.ok) throw new Error(); return response.json(); })
      .then((data) => setProviders(data))
      .catch(() => setProviders([]));
  }, []);

  function changeQuery(value) {
    setQuery(value);
    setResults([]);
    setError('');
    setResearch(null);
    setResearchError('');
  }

  async function discover(event) {
    event.preventDefault();
    const value = query.trim();
    if (!value || loading) return;
    setLoading(true); setError(''); setResults([]); setResearch(null); setResearchError('');
    try {
      const response = await fetch('/api/v1/discovery?query=' + encodeURIComponent(value));
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Discovery request failed.');
      setResults(data.results || []);
      if (!data.results?.length) setError('No results were returned. Try a more specific engineering question.');
    } catch (err) { setError(err.message || 'Unable to discover resources right now.'); }
    finally { setLoading(false); }
  }

  async function runResearch() {
    const value = query.trim();
    if (!value || researching) return;
    setResearching(true); setResearchError(''); setResearch(null);
    try {
      const response = await fetch('/api/v1/research', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: value })
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Research request failed.');
      setResearch(data);
    } catch (err) { setResearchError(err.message || 'Unable to research this decision right now.'); }
    finally { setResearching(false); }
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="/" aria-label="TELYVO home"><span className="brand-mark"><HubRoundedIcon /></span><span>TELYVO<span className="brand-period">.</span></span></a>
        <div className="workspace-label">WORKSPACE</div>
        <nav className="side-nav">
          <a className="nav-item active" href="#discover"><SearchRoundedIcon /> Discover</a>
          <a className="nav-item" href="#research"><AutoAwesomeRoundedIcon /> Research</a>
          <a className="nav-item" href="#discuss"><ForumRoundedIcon /> Discussions</a>
        </nav>
        <div className="sidebar-bottom"><div className="api-indicator"><span className={apiStatus === 'API online' ? 'status-dot online' : 'status-dot'} />{apiStatus}</div><div className="version-label">MVP BUILD · 0.3.0</div></div>
      </aside>
      <section className="main-panel">
        <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <strong>Technology discovery</strong></div><div className="topbar-right"><span className="live-pill"><span /> MVP workspace</span><div className="provider-pills">{providers.map((provider) => <span className={provider.configured ? "provider-pill configured" : "provider-pill"} key={provider.provider} title="Credential presence only; not a live provider health check">{provider.provider} · {provider.configured ? "configured" : "missing key"}</span>)}</div></div></header>
        <div className="content">
          <section className="hero" id="discover">
            <div className="eyebrow"><span className="eyebrow-line" /> ENGINEERING DECISIONS, GROUNDED IN EVIDENCE</div>
            <h1>Make your next<br /><span>technology decision</span><br />with confidence.</h1>
            <p className="hero-copy">Discover what is changing. Research what matters. Discuss the evidence with your team.</p>
            <form className="search-card" onSubmit={discover}>
              <div className="search-label">WHAT ARE YOU DECIDING?</div>
              <div className="search-row"><SearchRoundedIcon className="search-icon" /><input value={query} onChange={(event) => changeQuery(event.target.value)} placeholder="e.g. Spring Boot vs Go for microservices" aria-label="Technology decision question" maxLength={240} disabled={loading || researching} /><button type="submit" disabled={!query.trim() || loading}>{loading ? 'Searching…' : 'Discover'} <ArrowForwardRoundedIcon /></button></div>
              <div className="search-foot"><span>Live web discovery · SerpApi</span><span>{query.length}/240</span></div>
            </form>
            <div className="suggestion-row"><span>TRY:</span><button type="button" disabled={loading || researching} onClick={() => changeQuery('Spring Boot vs Go for microservices')}>Spring Boot vs Go</button><button type="button" onClick={() => changeQuery('PostgreSQL vs MongoDB for SaaS')}>PostgreSQL vs MongoDB</button><button type="button" onClick={() => changeQuery('Kubernetes vs serverless in 2026')}>Kubernetes vs serverless</button></div>
          </section>

          {(error || results.length > 0) && <section className="results-section" aria-live="polite"><div className="section-heading"><div><span className="section-kicker">DISCOVERY RESULTS · SERPAPI</span><h2>{results.length} sources found</h2></div><span className="section-note">Provider-returned results</span></div>{error && <p className="error-message" role="alert">{error}</p>}<div className="results-grid">{results.map((item, index) => <article className="result-card" key={item.url || index}><span className="result-source">{item.source || 'WEB RESULT'}{item.publishedAt ? ' · ' + item.publishedAt : ''}</span><h3><a href={item.url} target="_blank" rel="noreferrer">{item.title}</a></h3><p>{item.snippet}</p><a className="result-link" href={item.url} target="_blank" rel="noreferrer">Open source <ArrowForwardRoundedIcon /></a></article>)}</div>{query.trim() && <button className="research-button" onClick={runResearch} disabled={researching}>{researching ? 'Researching with You.com…' : 'Synthesize evidence with You.com'} <AutoAwesomeRoundedIcon /></button>}</section>}

          {(research || researchError || researching) && <section className="research-section" id="research" aria-live="polite"><div className="section-heading"><div><span className="section-kicker">GROUNDED RESEARCH · YOU.COM</span><h2>Evidence synthesis.</h2></div><span className="section-note">{researching ? 'Research in progress' : research ? `${research.sources?.length || 0} cited sources` : 'Research unavailable'}</span></div>{researchError && <p className="error-message" role="alert">{researchError}</p>}{researching && <p className="research-loading">You.com is searching, reading and synthesizing sources. This can take several seconds.</p>}{research && <><article className="research-answer"><div className="research-answer-label">RESEARCH ANSWER</div><div className="research-markdown"><ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeSanitize]} components={{ a: ({ node, ...props }) => <a {...props} target={props.href?.startsWith('#') ? undefined : '_blank'} rel={props.href?.startsWith('#') ? undefined : 'noreferrer'} /> }}>{linkResearchCitations(research.answer)}</ReactMarkdown></div></article><div className="research-sources"><h3>Sources returned by You.com</h3>{(research.sources || []).map((source, index) => <article className="research-source" id={`research-source-${source.sourceIndex ?? index + 1}`} key={source.url}><span>[{source.sourceIndex ?? index + 1}]</span><div><a href={source.url} target="_blank" rel="noreferrer">{source.title}</a>{source.snippets?.[0] && <p>{source.snippets[0]}</p>}</div></article>)}</div></>}</section>}

          <section className="workflow-section"><div className="section-heading"><div><span className="section-kicker">THE WORKFLOW</span><h2>From question to decision.</h2></div><span className="section-note">Three connected stages</span></div><div className="workflow-grid">{stages.map((stage) => <article className="workflow-card" key={stage.number} id={stage.title.toLowerCase()}><div className="card-top"><span>{stage.number} / 03</span><span className="card-icon">{stage.icon}</span></div><h3>{stage.title}</h3><p>{stage.description}</p><span className="integration-tag">{stage.tag}</span></article>)}</div></section>
          <section className="demo-callout"><div className="demo-icon"><TerminalRoundedIcon /></div><div><span className="section-kicker">DECISION WORKSPACE</span><h3>Move from search results to a team decision.</h3><p>Discover and research are connected to live providers. Discuss uses the Nexconn Chat SDK.</p></div><span className="demo-arrow"><ArrowForwardRoundedIcon /></span></section>
          {research && <DecisionLog query={query} research={research} />}
          <ChatRoom />
          <footer className="footer"><span>TELYVO <span className="brand-period">.</span> Evidence over assumption.</span><span>Built for engineering teams</span></footer>
        </div>
      </section>
    </main>
  );
}