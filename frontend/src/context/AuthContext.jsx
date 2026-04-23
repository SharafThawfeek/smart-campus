import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/services';

/**
 * Authentication Context.
 * Manages user state, login/logout, and token persistence.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // On mount, verify existing token
  useEffect(() => {
    const verifyToken = async () => {
      if (token) {
        try {
          const response = await authAPI.getMe();
          setUser(response.data);
        } catch {
          // Token is invalid, clear everything
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    };
    verifyToken();
  }, [token]);

  /**
   * Login with Google OAuth data.
   * Sends user info to the backend and receives a JWT token.
   */
  const login = async (googleData) => {
    try {
      const response = await authAPI.googleLogin({
        email: googleData.email,
        name: googleData.name,
        picture: googleData.picture,
      });

      const { token: jwtToken, user: userData } = response.data;

      localStorage.setItem('token', jwtToken);
      localStorage.setItem('user', JSON.stringify(userData));
      setToken(jwtToken);
      setUser(userData);

      return userData;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  /** Logout: clear token and user data */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  /** Check if the current user is an admin */
  const isAdmin = () => user?.role === 'ADMIN';

  /** Check if the current user is a technician */
  const isTechnician = () => user?.role === 'TECHNICIAN';

  return (
    <AuthContext.Provider
      value={{ user, token, loading, login, logout, isAdmin, isTechnician }}
    >
      {children}
    </AuthContext.Provider>
  );
}

/** Custom hook to access auth context */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
