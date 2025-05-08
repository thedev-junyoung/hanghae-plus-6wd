import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 20,
    duration: '10s',
};

const API_BASE_URL = 'http://host.docker.internal:8080';

export default function () {
    const res = http.get(`${API_BASE_URL}/api/v1/products/popular/without-cache`, {
        headers: {
            'X-USER-ID': '100',
        },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
