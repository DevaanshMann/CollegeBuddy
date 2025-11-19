import { useNavigate } from "react-router-dom";
import logo from "../../assets/CollegeBuddy.png";

export function LandingPage() {
    const navigate = useNavigate();

    const handleLogoClick = () => {
        navigate("/login");
    };

    return (
        <div
            style={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                minHeight: "100vh",
                flexDirection: "column",
                backgroundColor: "#ffffff",
            }}
        >
            <img
                src={logo}
                alt="CollegeBuddy Logo"
                onClick={handleLogoClick}
                style={{
                    maxWidth: "500px",
                    width: "100%",
                    cursor: "pointer",
                    transition: "transform 0.2s ease-in-out",
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.transform = "scale(1.05)";
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.transform = "scale(1)";
                }}
            />
            <p
                style={{
                    marginTop: "2rem",
                    color: "#666",
                    fontSize: "1.1rem",
                    textAlign: "center",
                }}
            >
                Click the logo to get started
            </p>
        </div>
    );
}
