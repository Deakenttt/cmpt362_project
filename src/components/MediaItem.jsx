const MediaItem = ({ item }) => {
    const cardStyle = {
        padding: "20px",
        borderRadius: "10px",
        backgroundColor: "#fff",
        boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
        textAlign: "center",
        width: "480px", // Increased width for consistency
        margin: "10px auto",
    };
  
    const mediaStyle = {
        width: "100%", // Maintain the full width of the card
        height: "auto", // Automatically adjust height to keep aspect ratio
        borderRadius: "8px",
        marginBottom: "10px",
    };
  
    const iframeStyle = {
        width: "100%",
        height: "calc(450px * 10 / 16)", // Maintain 9:16 aspect ratio
        border: "none",
        borderRadius: "8px",
    };
  
    const linkStyle = {
      color: "var(--primary-color)",
      fontSize: "1.2em",
      fontWeight: "bold",
      textDecoration: "none",
    };
  
    const linkHoverStyle = {
      textDecoration: "underline",
    };
  
    // Handle Images
    if (item.type === "image") {
      return (
        <div style={cardStyle}>
          <img src={item.url} alt={item.title} style={mediaStyle} />
          <p>{item.title}</p>
        </div>
      );
    }
    // Handle Videos
    else if (item.type === "video") {
      return (
        <div style={cardStyle}>
          <video controls style={mediaStyle}>
            <source src={item.url} type="video/mp4" />
          </video>
          <p>{item.title}</p>
        </div>
      );
    }
    // Handle YouTube Videos
    else if (item.type === "youtube") {
      // Extract video ID from the YouTube URL
      const videoId = new URL(item.url).searchParams.get("v");
      return (
        <div style={cardStyle}>
          <iframe
            style={iframeStyle}
            src={`https://www.youtube.com/embed/${videoId}`}
            title={item.title}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          ></iframe>
          <p>{item.title}</p>
        </div>
      );
    }
    // Handle Links
    else if (item.type === "link") {
      return (
        <div style={cardStyle}>
          <a
            href={item.url}
            target="_blank"
            rel="noopener noreferrer"
            style={linkStyle}
            onMouseOver={(e) => (e.currentTarget.style.textDecoration = linkHoverStyle.textDecoration)}
            onMouseOut={(e) => (e.currentTarget.style.textDecoration = "none")}
          >
            {item.title}
          </a>
        </div>
      );
    }
  
    // Fallback for unknown types
    return null;
  };
  
  export default MediaItem;
  