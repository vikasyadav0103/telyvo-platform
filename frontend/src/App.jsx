import { useEffect, useState } from 'react';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import AutoAwesomeRoundedIcon from '@mui/icons-material/AutoAwesomeRounded';
import ForumRoundedIcon from '@mui/icons-material/ForumRounded';
import TravelExploreRoundedIcon from '@mui/icons-material/TravelExploreRounded';
import TerminalRoundedIcon from '@mui/icons-material/TerminalRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import HubRoundedIcon from '@mui/icons-material/HubRounded';

const stages = [
  { number: '01', title: 'Discover', description: 'Find current engineering resources.', icon: <TravelExploreRoundedIcon />, tag: 'SERPAPI' },
  { number: '02', title: 'Research', description: 'Compare options with cited evidence.', icon: <AutoAwesomeRoundedIcon />, tag: 'YOU.COM' },
  { number: '03', title: 'Discuss', description: 'Bring the team into the decision.', icon: <ForumRoundedIcon />, tag: 'NEXCONN' }
];

export default function App() {
  const [query, setQuery] = useState('');
  const [apiStatus, setApiStatus] = useState('Checking API');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetch('/api/v1/health')
      .then((response) => { if (!response.ok) throw new Error(); return response.json(); })
      .then((data) => setApiStatus(data.status === 'UP' ? 'API online' : 'API unavailable'))
      .catch(() => setApiStatus('API offline'));
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    const value = query.trim();
    if (!value || loading) return;
    setLoading(true); setError(''); setResults([]);
    try {
      const response = await fetch('/api/v1/discovery?query=' + encodeURIComponent(value));
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Discovery request failed.');
      setResults(data.results || []);
      if (!data.results?.length) setError('No results were returned. Try a more specific engineering question.');
    } catch (err) {
      setError(err.message || 'Unable to discover resources right now.');
    } finally { setLoading(false); }
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
        <div className="sidebar-bottom"><div className="api-indicator"><span className={apiStatus === 'API online' ? 'status-dot online' : 'status-dot'} />{apiStatus}</div><div className="version-label">MVP BUILD · 0.2.0</div></div>
      </aside>
      <section className="main-panel">
        <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <strong>Technology discovery</strong></div><div className="topbar-right"><span className="live-pill"><span /> MVP workspace</span></div></header>
        <div className="content">
          <section className="hero" id="discover">
            <div className="eyebrow"><span className="eyebrow-line" /> ENGINEERING DECISIONS, GROUNDED IN EVIDENCE</div>
            <h1>Make your next<br /><span>technology decision</span><br />with confidence.</h1>
            <p className="hero-copy">Discover what is changing. Research what matters. Discuss the evidence with your team.</p>
            <form className="search-card" onSubmit={handleSubmit}>
              <div className="search-label">WHAT ARE YOU DECIDING?</div>
              <div className="search-row"><SearchRoundedIcon className="search-icon" /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="e.g. Spring Boot vs Go for microservices" aria-label="Technology decision question" maxLength={240} /><button type="submit" disabled={!query.trim() || loading}>{loading ? 'Searching…' : 'Explore'} <ArrowForwardRoundedIcon /></button></div>
              <div className="search-foot"><span>Live web discovery · SerpApi</span><span>{query.length}/240</span></div>
            </form>
            <div className="suggestion-row"><span>TRY:</span><button type="button" onClick={() => setQuery('Spring Boot vs Go for microservices')}>Spring Boot vs Go</button><button type="button" onClick={() => setQuery('PostgreSQL vs MongoDB for SaaS')}>PostgreSQL vs MongoDB</button><button type="button" onClick={() => setQuery('Kubernetes vs serverless in 2026')}>Kubernetes vs serverless</button></div>
          </section>
          {(error || results.length > 0) && <section className="results-section" aria-live="polite"><div className="section-heading"><div><span className="section-kicker">DISCOVERY RESULTS</span><h2>{results.length} sources found</h2></div></div>{error && <p className="error-message" role="alert">{error}</p>}<div className="results-grid">{results.map((item, index) => <article className="result-card" key={item.url || index}><span className="result-source">{item.source || 'WEB RESULT'}{item.publishedAt ? ' · ' + item.publishedAt : ''}</span><h3><a href={item.url} target="_blank" rel="noreferrer">{item.title}</a></h3><p>{item.snippet}</p><a className="result-link" href={item.url} target="_blank" rel="noreferrer">Open source <ArrowForwardRoundedIcon /></a></article>)}</div></section>}
          <section className="workflow-section"><div className="section-heading"><div><span className="section-kicker">THE WORKFLOW</span><h2>From question to decision.</h2></div><span className="section-note">Three connected stages</span></div><div className="workflow-grid">{stages.map((stage) => <article className="workflow-card" key={stage.number} id={stage.title.toLowerCase()}><div className="card-top"><span>{stage.number} / 03</span><span className="card-icon">{stage.icon}</span></div><h3>{stage.title}</h3><p>{stage.description}</p><span className="integration-tag">{stage.tag}</span></article>)}</div></section>
          <section className="demo-callout"><div className="demo-icon"><TerminalRoundedIcon /></div><div><span className="section-kicker">NEXT INVESTIGATION</span><h3>Research a decision, then discuss it with your team.</h3><p>Live discovery is connected. Evidence synthesis and collaboration are the next integration milestones.</p></div><span className="demo-arrow"><ArrowForwardRoundedIcon /></span></section>
          <footer className="footer"><span>TELYVO <span className="brand-period">.</span> Evidence over assumption.</span><span>Built for engineering teams</span></footer>
        </div>
      </section>
    </main>
  );
}