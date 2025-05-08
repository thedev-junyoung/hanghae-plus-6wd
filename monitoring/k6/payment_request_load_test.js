import http from 'monitoring/k6/http';
import { check } from 'k6';

export const options = {
    vus: 50, // 동시에 50명의 유저가
    duration: '10s', // 10초 동안 부하를 걸어
};

export default function () {
    const url = 'http://localhost:8080/api/balance/charge'; // 너의 충전 API 엔드포인트

    const payload = JSON.stringify({
        userId: __VU, // 가상 유저 ID, __VU = Virtual User 번호
        amount: 10000, // 1만원 충전
        reason: "부하테스트용 충전",
        requestId: `REQ-${__VU}-${__ITER}`, // 고유한 requestId 생성
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
