import { useEffect, useState } from 'react';
import { resourceAPI } from '../api/services';
import './Pages.css';

/**
 * Resources page (Module A).
 * Users can browse and filter campus resources (rooms, labs, equipment).
 * @author mohsh
 */
export default function Resources() {
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ type: '', status: '', location: '' });

  const fetchResources = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filters.type) params.type = filters.type;
      if (filters.status) params.status = filters.status;
      if (filters.location) params.location = filters.location;
      const res = await resourceAPI.getAll(params);
      setResources(res.data);
    } catch (err) {
      console.error('Failed to fetch resources:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchResources(); }, []);

  const handleFilter = () => fetchResources();

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Resources</h1>
          <p className="page-subtitle">Browse available campus facilities and equipment</p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="form-group">
          <label className="form-label">Type</label>
          <select className="form-select" value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}>
            <option value="">All Types</option>
            <option value="ROOM">Room</option>
            <option value="LAB">Lab</option>
            <option value="EQUIPMENT">Equipment</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-select" value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="OUT_OF_SERVICE">Out of Service</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Location</label>
          <input className="form-input" placeholder="Search location..." value={filters.location} onChange={(e) => setFilters({ ...filters, location: e.target.value })} />
        </div>
        <div className="form-group" style={{ justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={handleFilter}>🔍 Search</button>
        </div>
      </div>

      {/* Resources Grid */}
      {loading ? (
        <div className="grid-3">{[1,2,3,4,5,6].map(i => <div key={i} className="skeleton skeleton-card" style={{height:180}} />)}</div>
      ) : resources.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🏢</div>
          <div className="empty-state-title">No resources found</div>
          <p>Try adjusting your filters</p>
        </div>
      ) : (
        <div className="grid-3">
          {resources.map((r) => (
            <div key={r.resourceId} className="glass-card resource-card">
              <div className="resource-card-header">
                <span className="resource-type-icon">
                  {r.type === 'ROOM' ? '🏫' : r.type === 'LAB' ? '🔬' : '📽️'}
                </span>
                <span className={`badge badge-${r.status?.toLowerCase()}`}>{r.status}</span>
              </div>
              <h3 className="resource-name">{r.name}</h3>
              <div className="resource-details">
                <div className="resource-detail">
                  <span className="text-muted">Type:</span> {r.type}
                </div>
                {r.capacity && (
                  <div className="resource-detail">
                    <span className="text-muted">Capacity:</span> {r.capacity} people
                  </div>
                )}
                {r.location && (
                  <div className="resource-detail">
                    <span className="text-muted">Location:</span> {r.location}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
