import json
import random
import time
from datetime import datetime, timedelta

airports = ["AJL", "AMD", "BBS", "BLR", "BOM", "CCU", "DED", "DEL", "GAU", "HYD", "IMF", "IXA", "IXC", "IXR", "LKO", "MAA", "PAT", "SHL"]
airlines = [
    {"code": "6E", "name": "IndiGo"},
    {"code": "AI", "name": "Air India"},
    {"code": "UK", "name": "Vistara"},
    {"code": "QP", "name": "Akasa Air"}
]

# Time categories based on rules: morning: 00-11, afternoon: 12-17, evening: 18-23
def get_time_category(hour):
    if 0 <= hour <= 11:
        return "morning"
    elif 12 <= hour <= 17:
        return "afternoon"
    else:
        return "evening"

# We'll generate flights for a specific future date, let's say tomorrow.
base_date = datetime.now() + timedelta(days=1)
base_date = base_date.replace(hour=0, minute=0, second=0, microsecond=0)

flights_data = {}

def random_time_in_range(start_hour, end_hour):
    h = random.randint(start_hour, end_hour)
    m = random.randint(0, 59)
    return base_date.replace(hour=h, minute=m)

for source in airports:
    for dest in airports:
        if source == dest:
            continue
        
        # 6 flights per route: 2 morning, 2 afternoon, 2 evening
        # We need at least 2 non-stop, 2 one-stop
        stops_distribution = [0, 0, 1, 1, random.choice([0, 1]), random.choice([0, 1])]
        random.shuffle(stops_distribution)
        
        # Generate 2 morning (00-11)
        # Generate 2 afternoon (12-17)
        # Generate 2 evening (18-23)
        time_slots = [
            (0, 11), (0, 11),
            (12, 17), (12, 17),
            (18, 23), (18, 23)
        ]
        
        for i in range(6):
            airline = random.choice(airlines)
            stops = stops_distribution[i]
            is_non_stop = (stops == 0)
            
            # Duration must vary (90 - 200 min)
            duration_min = random.randint(90, 200)
            
            # Prices must vary (2000 - 9000). Some one-stop flights must be cheaper than non-stop
            # If stops == 1, maybe make it cheaper on average, but add randomness
            if stops == 1:
                price = random.randint(2000, 7000)
            else:
                price = random.randint(3500, 9000)
                
            seats = random.randint(10, 180)
            
            dep_time = random_time_in_range(time_slots[i][0], time_slots[i][1])
            arr_time = dep_time + timedelta(minutes=duration_min)
            
            time_category = get_time_category(dep_time.hour)
            
            timestamp_ms = int(dep_time.timestamp() * 1000)
            doc_id = f"{airline['code']}_{source}_{dest}_{timestamp_ms}"
            
            # Timestamp in milliseconds
            flight = {
                "airline": airline["code"],
                "airline_name": airline["name"],
                "source": source,
                "destination": dest,
                "departure_time": timestamp_ms,
                "arrival_time": int(arr_time.timestamp() * 1000),
                "duration": duration_min,
                "price": price,
                "stops": stops,
                "is_non_stop": is_non_stop,
                "seats_available": seats,
                "time_category": time_category
            }
            flights_data[doc_id] = flight

# Format ready for standard import scripts usually expect collection as top level
# Some tools want {"flights": {doc_id: data, ...}}
output = {"flights": flights_data}

with open("firestore_flights.json", "w") as f:
    json.dump(output, f, indent=2)

print(f"Generated {len(flights_data)} flights to firestore_flights.json")
