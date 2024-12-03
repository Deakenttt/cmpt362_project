import React, { useEffect, useState } from "react";
import MediaItem from "./components/MediaItem";
import pictures from "./data/pictures.json";
import videos from "./data/videos.json";
import links from "./data/links.json";
import works from "./data/works.json";
import './App.css';

const App = () => {
  const [activeSection, setActiveSection] = useState("pictures");

  // Scroll-Spy Effect
  useEffect(() => {
    const handleScroll = () => {
      const sections = ["pictures", "videos", "works"];
      const offsets = sections.map((id) => document.getElementById(id)?.offsetTop || 0);
      const scrollPosition = window.scrollY + 200; // Adjust offset to detect active section
      const activeIndex = offsets.findIndex((offset, i) => scrollPosition >= offset && scrollPosition < (offsets[i + 1] || Infinity));
      setActiveSection(sections[activeIndex]);
    };

    window.addEventListener("scroll", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, []);

  return (
    <div>
      {/* Navigation Bar */}
      <nav style={navStyle}>
        <ul style={navListStyle}>
          {["pictures", "videos", "works"].map((section) => (
            <li key={section} style={navItemStyle}>
              <a
                href={`#${section}`}
                style={{
                  ...navLinkStyle,
                  color: activeSection === section ? "var(--primary-color)" : "black",
                }}
                onClick={() => setActiveSection(section)}
              >
                {section.charAt(0).toUpperCase() + section.slice(1)}
              </a>
            </li>
          ))}
        </ul>
      </nav>

      {/* Header Section */}
      <header style={headerStyle}>
        <h1 style={headerTitleStyle}>Welcome to Our Match Maker</h1>
        <p style={headerDescriptionStyle}>
          This is the header to describle our App.
        </p>
      </header>

      {/* Main Content */}
      <div style={{ maxWidth: "1200px", margin: "0 auto", padding: "20px" }}>
        {/* Pictures Section */}
        <section id="pictures" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Pictures</h2>
          <div style={sectionContentStyle}>
            {pictures.map((item, index) => (
              <MediaItem key={index} item={item} />
            ))}
          </div>
        </section>

        {/* Videos Section */}
        <section id="videos" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Videos</h2>
          <div style={videosContentStyle}>
            {/* Display Videos */}
            <div style={mediaGroupStyle}>
              {videos.map((item, index) => (
                <MediaItem key={index} item={item} />
              ))}
            </div>
            {/* Display Links */}
            <div style={linkGroupStyle}>
              {links.map((item, index) => (
                <MediaItem key={index} item={item} />
              ))}
            </div>
          </div>
        </section>

        {/* Works Section */}
        <section id="works" style={sectionStyle}>
          <h2 style={sectionTitleStyle}>Works</h2>
          <div style={worksContentStyle}>
            {works.map((item, index) => (
              <MediaItem key={index} item={item} />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

// Navigation Bar Styles
const navStyle = {
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  backgroundColor: "#fff",
  boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
  zIndex: 1000,
};

const navListStyle = {
  display: "flex",
  justifyContent: "center",
  margin: 0,
  padding: "10px 0",
  listStyle: "none",
};

const navItemStyle = {
  margin: "0 15px",
};

const navLinkStyle = {
  textDecoration: "none",
  fontSize: "1.2em",
  fontWeight: "bold",
};

// Header Styles
const headerStyle = {
  textAlign: "center",
  margin: "50px auto 30px",
  padding: "20px",
};

const headerTitleStyle = {
  fontSize: "2.5em",
  marginBottom: "10px",
  color: "var(--primary-color)",
};

const headerDescriptionStyle = {
  fontSize: "1.2em",
  color: "#666",
};

// Section Styles
const sectionStyle = {
  marginBottom: "50px",
  paddingTop: "70px", // Offset for fixed navbar
};

const sectionTitleStyle = {
  fontSize: "2em",
  marginBottom: "20px",
  color: "var(--primary-color)",
  textAlign: "center",
};

const sectionContentStyle = {
  display: "flex",
  flexWrap: "wrap",
  gap: "30px",
  justifyContent: "center",
};

const videosContentStyle = {
  display: "flex",
  flexDirection: "column", // Stack videos and links vertically
  gap: "20px",
  alignItems: "center",
};

const mediaGroupStyle = {
  display: "flex",
  flexWrap: "wrap",
  gap: "20px",
  justifyContent: "center",
};

const linkGroupStyle = {
  display: "flex",
  flexDirection: "column", // Links are stacked
  alignItems: "center",
  gap: "10px",
};

const worksContentStyle = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
  gap: "20px",
  justifyItems: "center",
};

export default App;
