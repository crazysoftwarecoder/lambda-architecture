# Real-Time Product Event Analytics System

This project implements a real-time analytics system for tracking and visualizing e-commerce product events using a Lambda Architecture approach.

## System Overview

The system tracks three types of product events:
- Product Views
- Add to Cart Actions
- Purchases

### Architecture Components

1. **Event Generation (Java)**
   - Simulates product events
   - Publishes events to Kafka topics
   - Located in `src/main/java/org/emitters`

2. **Stream Processing (Apache Spark)**
   - Processes events in real-time using Spark Structured Streaming
   - Aggregates event counts by product and event type
   - Stores results in PostgreSQL

3. **Data Storage**
   - **Kafka**: Message broker for event streaming
   - **PostgreSQL**: Stores aggregated event counts

4. **Visualization (Flask + Chart.js)**
   - Real-time dashboard showing event metrics
   - Updates every 5 seconds
   - Shows three charts for views, cart adds, and purchases

## Tech Stack

- **Backend**:
  - Java 17
  - Apache Spark 3.3.0
  - Apache Kafka
  - PostgreSQL

- **Frontend**:
  - Flask (Python)
  - Chart.js
  - HTML/CSS/JavaScript

- **Infrastructure**:
  - Docker
  - Docker Compose

## Getting Started

1. **Prerequisites**
   - Docker and Docker Compose
   - Java 17
   - Maven

2. **Running the Application**
   ```bash
   docker-compose up --build
   ```

3. **Accessing Components**
   - Dashboard: http://localhost:5001
   - AKHQ (Kafka UI): http://localhost:8080
   - pgAdmin: http://localhost:5050
     - Email: admin@admin.com
     - Password: admin

## Usage

1. Open the dashboard at http://localhost:5001
2. Enter a product ID in the input field
3. Click "Track Product" to start monitoring
4. The charts will automatically update every 5 seconds
5. Click "Stop Tracking" to end monitoring

## Project Structure
