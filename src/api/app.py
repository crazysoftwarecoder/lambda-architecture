from flask import Flask, render_template, jsonify
import psycopg2
from psycopg2.extras import RealDictCursor
import os

app = Flask(__name__)

def get_db_connection():
    return psycopg2.connect(
        host=os.getenv('DB_HOST', 'localhost'),
        database=os.getenv('DB_NAME', 'postgres'),
        user=os.getenv('DB_USER', 'postgres'),
        password=os.getenv('DB_PASS', 'postgres')
    )

@app.route('/health')
def health():
    try:
        # Test database connection
        conn = get_db_connection()
        conn.close()
        return jsonify({"status": "healthy"}), 200
    except Exception as e:
        return jsonify({"status": "unhealthy", "error": str(e)}), 500

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/product/<product_id>')
def get_product_counts(product_id):
    conn = get_db_connection()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    
    cur.execute("""
        SELECT "eventType" as event_type, 
               SUM("eventCount") as count
        FROM event_counts
        WHERE product = %s
        GROUP BY "eventType"
    """, (product_id,))
    
    results = cur.fetchall()
    
    # Initialize counts
    counts = {
        'viewed_count': 0,
        'cart_count': 0,
        'purchased_count': 0
    }
    
    # Update counts based on results
    for row in results:
        if row['event_type'] == 'PRODUCT_VIEWED':
            counts['viewed_count'] = row['count']
        elif row['event_type'] == 'PRODUCT_ADDED_TO_CART':
            counts['cart_count'] = row['count']
        elif row['event_type'] == 'PRODUCT_PURCHASED':
            counts['purchased_count'] = row['count']
    
    cur.close()
    conn.close()
    
    return jsonify(counts)

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)