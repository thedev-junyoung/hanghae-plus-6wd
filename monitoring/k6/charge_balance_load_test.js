import http from 'monitoring/k6/http';
import { check } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    vus: 20, // 20명
    duration: '10s', // 10초 동안 전부 요청
};

const userId = 100; // 전부 10000번 유저만 집중 공격
const API_BASE_URL = 'http://localhost:8080';

export default function () {
    const amount = Math.floor(Math.random() * 10000) + 1000; // 1000 ~ 10000
    const requestId = `req-${randomString(8)}-${Date.now()}`;

    const payload = JSON.stringify({
        userId: userId,
        amount: amount,
        requestId: requestId,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-USER-ID': userId.toString(),
        },
    };

    const res = http.post(`${API_BASE_URL}/api/v1/balances/charge`, payload, params);

    const success = check(res, {
        'status is 200 or 400/409': (r) => [200, 400, 409].includes(r.status),
    });

    if (!success) {
        console.error(`❌ Failed: userId=${userId}, amount=${amount}, requestId=${requestId}`);
    }
}
