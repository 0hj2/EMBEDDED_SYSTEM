# 🌡️ Raspberry Pi 4 온습도 모니터링 LED 시스템

본 프로젝트는 **라즈베리파이 4**를 사용하여 온도와 습도를 측정하고, 이를 LED 색상으로 시각화하며, CoAP 프로토콜을 통해 원격에서 데이터를 조회하고 제어할 수 있는 시스템입니다.

---

## 🔧 주요 기능

1. **DHT11 센서 온습도 측정**
   - GPIO를 통해 DHT11 센서 데이터 읽기
   - 온도와 습도를 CoAP 서버를 통해 제공
   - 센서 오류 발생 시 `-99` 값 반환

2. **LED 시각화**
   - 온습도에 따라 RGB LED 색상 변경
     - 온도 ≤ 0°C → 흰색
     - 온도 ≥ 30°C → 빨간색
     - 습도 ≥ 80% → 파란색
     - 그 외 → 초록색
     - 센서 오류 → LED OFF
   - GPIO를 통한 안전한 LED 제어

3. **CoAP 서버**
   - DHT11 센서와 LED를 리소스로 등록
   - 클라이언트 요청(GET, PUT, DELETE, OBSERVE) 처리
   - 센서 변화 시 Observe 기능으로 자동 알림 전송

4. **GUI 클라이언트**
   - Java Swing 기반 간단 GUI 제공
   - GET, PUT, OBSERVE, DELETE 기능 지원
   - 실시간 온습도 값 및 LED 색상 확인

---

## 📂 파일 구조
- CoAP.java # 서버 실행 메인 클래스
- DHT.java # DHT11 센서 CoAP 리소스
- DHT11_S.java # DHT11 센서 데이터 처리 클래스
- LED.java # RGB LED CoAP 리소스 및 제어
-  GUI.java # CoAP 클라이언트 GUI

---

## ⚙️ 설치 및 실행

### 1. 필요 라이브러리
- [Pi4J](https://pi4j.com/) (GPIO 제어)
- [WS4D CoAP](http://ws4d.org/coap/) (CoAP 프로토콜)

### 2. 서버 실행
- java -cp target/classes Final.CoAP

### 3. 클라이언트 실행 (GUI)
- java -cp target/classes Final.GUI

---

## 🖥️ CoAP 명령
| 명령      | 설명                |
| ------- | ----------------- |
| GET     | 현재 온습도 값 조회       |
| PUT     | 온습도 값 설정          |
| OBSERVE | 센서 값 변화를 실시간 모니터링 |
| DELETE  | 리소스 종료 및 관찰 해제    |

---

## 💡 LED 색상 기준
| 조건                        | LED 색상 |
| ------------------------- | ------ |
| 온도 ≤ 0°C                  | White  |
| 온도 ≥ 30°C                 | Red    |
| 습도 ≥ 80%                  | Blue   |
| 0 < 온도 < 30°C && 습도 < 80% | Green  |
| 센서 오류                     | Off    |


---
## 📝 참고 사항
- DHT11 센서는 15번 핀 사용
- GPIO 핀 배치는 Pi4J RaspiPin 기준
