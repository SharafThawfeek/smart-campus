import { useNavigate } from 'react-router-dom';
import { GoogleLogin, GoogleOAuthProvider } from '@react-oauth/google';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { jwtDecode } from './jwtHelper';
import './Login.css';

/**
 * Login page with Google OAuth Sign-In.
 * After successful authentication, user is redirected to the dashboard.
 * @author mohsh
 */
export default function Login() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  // If already logged in, redirect
  if (user) {
    navigate('/dashboard', { replace: true });
    return null;
  }

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      // Decode the Google JWT to extract user info
      const decoded = jwtDecode(credentialResponse.credential);

      await login({
        email: decoded.email,
        name: decoded.name,
        picture: decoded.picture,
      });

      toast.success('Welcome to CampusHub!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Login error:', error);
      toast.error('Login failed. Please try again.');
    }
  };

  const handleGoogleError = () => {
    toast.error('Google Sign-In failed. Please try again.');
  };

  // Demo login for development (without Google OAuth)
  const handleDemoLogin = async (role) => {
    try {
      await login({
        email: role === 'ADMIN' ? 'admin@campus.edu' : 'user@campus.edu',
        name: role === 'ADMIN' ? 'Admin User' : 'John Student',
        picture: null,
      });
      toast.success(`Logged in as ${role}`);
      navigate('/dashboard');
    } catch (error) {
      toast.error('Demo login failed. Is the backend running?');
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-icon">🏛️</div>
        <h1 className="login-title">CampusHub</h1>
        <p className="login-subtitle">Smart Campus Operations Hub</p>

        {/* Google OAuth Sign-In */}
        <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID || 'demo'}>
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={handleGoogleError}
            theme="filled_black"
            size="large"
            width="100%"
            text="signin_with"
          />
        </GoogleOAuthProvider>

        <div className="login-divider">or use demo accounts</div>

        {/* Demo Login Buttons */}
        <div className="flex flex-col gap-sm">
          <button className="btn btn-primary w-full" onClick={() => handleDemoLogin('USER')}>
            🎓 Login as Student (USER)
          </button>
          <button className="btn btn-secondary w-full" onClick={() => handleDemoLogin('ADMIN')}>
            🛡️ Login as Admin
          </button>
        </div>

        {/* Features List */}
        <div className="login-features">
          <div className="login-feature">
            <span className="login-feature-icon">✓</span>
            Book rooms, labs & equipment
          </div>
          <div className="login-feature">
            <span className="login-feature-icon">✓</span>
            Report & track incidents
          </div>
          <div className="login-feature">
            <span className="login-feature-icon">✓</span>
            Real-time notifications
          </div>
          <div className="login-feature">
            <span className="login-feature-icon">✓</span>
            Role-based dashboards
          </div>
        </div>
      </div>
    </div>
  );
}
