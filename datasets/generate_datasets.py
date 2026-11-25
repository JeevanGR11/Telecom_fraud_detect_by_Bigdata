import random
import datetime
import os

INDIAN_PREFIXES = ['98', '99', '97', '96', '94', '91', '87', '88', '89', '80', '81', '82', '70', '79', '78', '77', '63', '62']
CITIES = [
    ('Bengaluru', 'Karnataka'),
    ('Mysuru', 'Karnataka'),
    ('Chennai', 'Tamil Nadu'),
    ('Hyderabad', 'Telangana'),
    ('Mumbai', 'Maharashtra'),
    ('Pune', 'Maharashtra'),
    ('Delhi', 'Delhi'),
    ('Kochi', 'Kerala'),
    ('Kolkata', 'West Bengal'),
    ('Ahmedabad', 'Gujarat'),
    ('Jaipur', 'Rajasthan'),
    ('Lucknow', 'Uttar Pradesh'),
    ('Chandigarh', 'Punjab'),
    ('Bhopal', 'Madhya Pradesh')
]
CALL_TYPES = ['OUTGOING', 'INCOMING', 'INTERNATIONAL', 'MISSED', 'REJECTED']
NETWORK_TYPES = ['5G VoNR', '4G VoLTE', '4G LTE', '3G/WCDMA']

KNOWN_SUSPECTS = [
    ('9876543210', 'WANGIRI', '103.24.18.52'),
    ('8765432109', 'ROBOCALL', '49.207.12.90'),
    ('9123456780', 'TOLL_BYPASS', '122.161.45.10'),
    ('9988776655', 'HIGH_VOLUME', '115.240.10.14'),
    ('8765123490', 'NIGHT_BLAST', '103.45.19.22')
]

BENIGN_NUMBERS = [
    '9845012345',
    '9845123456',
    '9123456789',
    '9988771234',
    '9876501234'
]

def gen_phone():
    p = random.choice(INDIAN_PREFIXES)
    rem = ''.join([str(random.randint(0, 9)) for _ in range(8)])
    return p + rem

def generate_cdr(count=1000, output_path="datasets/sample-cdr.csv"):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    other_numbers = [gen_phone() for _ in range(max(50, count // 10))]
    all_pool = [s[0] for s in KNOWN_SUSPECTS] + BENIGN_NUMBERS + other_numbers
    
    records = []
    base_time = datetime.datetime(2026, 11, 20, 0, 0, 0)
    
    # 1. Wangiri suspect 9876543210 (short calls, late night, multiple receivers)
    wangiri_count = max(15, int(count * 0.025))
    for i in range(wangiri_count):
        rec_time = base_time + datetime.timedelta(
            days=random.randint(0, 3),
            hours=random.randint(1, 4),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )
        target = random.choice([n for n in all_pool if n != '9876543210'])
        c1 = ('Bengaluru', 'Karnataka')
        c2 = random.choice(CITIES)
        records.append({
            'record_id': f'CDR-{10000 + len(records)}',
            'caller': '9876543210',
            'receiver': target,
            'duration': random.randint(2, 5),
            'type': 'OUTGOING',
            'timestamp': rec_time.strftime('%Y-%m-%d %H:%M:%S'),
            'src': f'{c1[0]}, {c1[1]}',
            'dst': f'{c2[0]}, {c2[1]}',
            'ip': '103.24.18.52',
            'network': '5G VoNR'
        })

    # 2. Robocall suspect 8765432109 (high frequency daytime blasts)
    robocall_count = max(20, int(count * 0.03))
    for i in range(robocall_count):
        rec_time = base_time + datetime.timedelta(
            days=random.randint(0, 3),
            hours=random.randint(9, 17),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )
        target = random.choice([n for n in all_pool if n != '8765432109'])
        c1 = ('Delhi', 'Delhi')
        c2 = random.choice(CITIES)
        records.append({
            'record_id': f'CDR-{10000 + len(records)}',
            'caller': '8765432109',
            'receiver': target,
            'duration': random.randint(15, 30),
            'type': 'OUTGOING',
            'timestamp': rec_time.strftime('%Y-%m-%d %H:%M:%S'),
            'src': f'{c1[0]}, {c1[1]}',
            'dst': f'{c2[0]}, {c2[1]}',
            'ip': '49.207.12.90',
            'network': '4G VoLTE'
        })

    # 3. Toll bypass suspect 9123456780 (long duration international calls)
    toll_count = max(6, int(count * 0.01))
    for i in range(toll_count):
        rec_time = base_time + datetime.timedelta(
            days=random.randint(0, 3),
            hours=random.randint(0, 4),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )
        target = random.choice([n for n in all_pool if n != '9123456780'])
        c1 = ('Mumbai', 'Maharashtra')
        int_dst = random.choice(['London, UK', 'Dubai, UAE', 'Singapore', 'New York, USA', 'Frankfurt, Germany'])
        records.append({
            'record_id': f'CDR-{10000 + len(records)}',
            'caller': '9123456780',
            'receiver': target,
            'duration': random.randint(1200, 3600),
            'type': 'INTERNATIONAL',
            'timestamp': rec_time.strftime('%Y-%m-%d %H:%M:%S'),
            'src': f'{c1[0]}, {c1[1]}',
            'dst': int_dst,
            'ip': '122.161.45.10',
            'network': '5G VoNR'
        })

    # 4. Fill remainder with legitimate and mixed telecom traffic
    while len(records) < count:
        caller = random.choice(all_pool)
        receiver = random.choice([n for n in all_pool if n != caller])
        rec_time = base_time + datetime.timedelta(
            days=random.randint(0, 5),
            hours=random.randint(6, 23),
            minutes=random.randint(0, 59),
            seconds=random.randint(0, 59)
        )
        c1 = random.choice(CITIES)
        c2 = random.choice(CITIES)
        records.append({
            'record_id': f'CDR-{10000 + len(records)}',
            'caller': caller,
            'receiver': receiver,
            'duration': random.randint(10, 380),
            'type': random.choice(['INCOMING', 'OUTGOING', 'OUTGOING', 'MISSED', 'REJECTED']),
            'timestamp': rec_time.strftime('%Y-%m-%d %H:%M:%S'),
            'src': f'{c1[0]}, {c1[1]}',
            'dst': f'{c2[0]}, {c2[1]}',
            'ip': f'{random.randint(103,182)}.{random.randint(10,240)}.{random.randint(1,250)}.{random.randint(1,250)}',
            'network': random.choice(NETWORK_TYPES)
        })

    random.shuffle(records)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write('record_id,caller_number,receiver_number,duration,call_type,call_timestamp,source_region,destination_region,ip_address,network_type\n')
        for r in records:
            f.write(f"{r['record_id']},{r['caller']},{r['receiver']},{r['duration']},{r['type']},{r['timestamp']},{r['src']},{r['dst']},{r['ip']},{r['network']}\n")

    print(f"Generated {len(records)} records at {output_path}")

if __name__ == '__main__':
    generate_cdr(1000, "datasets/sample-cdr.csv")
