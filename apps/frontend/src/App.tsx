import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { NavBar } from "./components/NavBar";
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

function App() {
    const isAuthenticated = Boolean(localStorage.getItem(JWT_STORAGE_KEY));

    return (
        <BrowserRouter>
            <div style={{ padding: "1rem 2rem", maxWidth: "1200px", margin: "0 auto" }}>
                <NavBar />
                <main style={{ marginTop: "2rem" }}>
                    <Routes>
                        <Route
                            path="/"
                            element={<Navigate to={isAuthenticated ? "/profile" : "/login"} replace />}
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
        </BrowserRouter>
    );
}

export default App;