FROM python:3.9-slim

# Set the working directory
WORKDIR /app

# Copy app files to the container
COPY . /app
RUN chmod -R 755 /app

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the default Flask port
EXPOSE 8080

# Command to run the app
CMD ["gunicorn", "--bind", "0.0.0.0:8080", "prediction:app"]
