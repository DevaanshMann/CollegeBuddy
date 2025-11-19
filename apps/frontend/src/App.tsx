import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { NavBar } from "./components/NavBar";
import { LandingPage } from "./pages/Landing/LandingPage";
import { SignupPage } from "./pages/Auth/SignupPage";
import { LoginPage } from "./pages/Auth/LoginPage";
import { VerifyPage } from "./pages/Auth/VerifyPage";
import { ProfilePage } from "./pages/Profile/ProfilePage";
import { SearchPage } from "./pages/Search/SearchPage";
import { ConnectionsPage } from "./pages/Connections/ConnectionsPage";
import { ChatPage } from "./pages/Chat/ChatPage";
import { JWT_STORAGE_KEY } from "./config";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const isAuthenticated = Boolean(localStorage.getItem(JWT_STORAGE_KEY));

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}

function AppContent() {
    const location = useLocation();
    const isAuthenticated = Boolean(localStorage.getItem(JWT_STORAGE_KEY));
    const showNavBar = location.pathname !== "/" || isAuthenticated;

    return (
        <div style={{ padding: showNavBar ? "1rem 2rem" : "0", maxWidth: "1200px", margin: "0 auto" }}>
            {showNavBar && <NavBar />}
            <main style={{ marginTop: showNavBar ? "2rem" : "0" }}>
                <Routes>
                    <Route
                        path="/"
                        element={isAuthenticated ? <Navigate to="/profile" replace /> : <LandingPage />}
                    />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/verify" element={<VerifyPage />} />

                    {/* Protected routes */}
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/search"
                        element={
                            <ProtectedRoute>
                                <SearchPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/connections"
                        element={
                            <ProtectedRoute>
                                <ConnectionsPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/chat/:otherUserId"
                        element={
                            <ProtectedRoute>
                                <ChatPage />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </main>
        </div>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
}

export default App;