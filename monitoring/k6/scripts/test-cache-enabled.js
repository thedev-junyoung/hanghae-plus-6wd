import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 20,
    duration: '10s',
};

const API_BASE_URL = 'http://host.docker.internal:8080'; // 도커 내부에서 접근
const userId = 100;

export default function () {
    const headers = {
        'X-USER-ID': userId.toString(),
    };

    const res = http.get(`${API_BASE_URL}/api/v1/products/popular`, { headers });

    const success = check(res, {
        'status is 200': (r) => r.status === 200,
    });

    if (!success) {
        console.error(`❌ Failed: userId=${userId}, status=${res.status}, body=${res.body}`);
    }
}
