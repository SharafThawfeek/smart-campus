import { useEffect, useState } from 'react';
import { authAPI } from '../../api/services';
import { useToast } from '../../context/ToastContext';
import '../Pages.css';

/**
 * Admin User Management page.
 * Admins can view all users and update their roles.
 */
export default function AdminUsers() {
  const toast = useToast();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await authAPI.getAllUsers();
      setUsers(res.data);
    } catch (err) {
      console.error('Failed to fetch users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchUsers(); }, []);

  const handleRoleChange = async (userId, newRole) => {
    try {
      await authAPI.updateUserRole(userId, newRole);
      toast.success('User role updated');
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update role');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">{users.length} registered user{users.length !== 1 ? 's' : ''}</p>
        </div>
      </div>

      {loading ? (
        <div>{[1,2,3].map(i => <div key={i} className="skeleton" style={{height:50,marginBottom:8}} />)}</div>
      ) : (
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <table className="data-table">
            <thead>
              <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.userId}>
                  <td>#{u.userId}</td>
                  <td style={{fontWeight:600,color:'var(--text-primary)'}}>
                    <div className="flex items-center gap-sm">
                      {u.profilePicture && (
                        <img src={u.profilePicture} alt="" style={{width:28,height:28,borderRadius:'50%'}} referrerPolicy="no-referrer" />
                      )}
                      {u.name}
                    </div>
                  </td>
                  <td>{u.email}</td>
                  <td>
                    <span className={`badge badge-${u.role === 'ADMIN' ? 'open' : u.role === 'TECHNICIAN' ? 'in_progress' : 'approved'}`}>
                      {u.role}
                    </span>
                  </td>
                  <td>
                    <select className="form-select" value={u.role} onChange={(e) => handleRoleChange(u.userId, e.target.value)} style={{width:'auto',padding:'4px 28px 4px 8px',fontSize:'0.8rem'}}>
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                      <option value="TECHNICIAN">TECHNICIAN</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
