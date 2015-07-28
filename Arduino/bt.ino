/*
PROTOCOLO DE COMANDOS:
2C -- ATUADOR
NC -- COMANDO
1C -- FIM COMANDO , OU ;
EX:
A11,   -- LIGA ATUADOR 1
A10;   -- DESLIGA ATUADOR 1
A21;   -- LIGA ATUADOR 1
A20,   -- DESLIGA ATUADOR 1
S1190; -- MOVIMENTA O SERVO ATE 90GRAUS
S15;   -- LIGA MOVIMENTA O SERVO EM LOOP
S16;   -- DESLIGA MOVIMENTA O SERVO EM LOOP


BT
TXD -- PINO 10
RXD -- PINO 11
GND -- GND
VCC -- 5V

SERVO
AMARELO  -- PINO 4
VERMELHO -- 5V
MARRON   -- GND

ATUADOR 1

ATUADOR 2
*/

#include <SoftwareSerial.h>
#include <Servo.h>
SoftwareSerial bt(10, 11); // TX, TX
String command = "";
int atuador1 = 2;
int atuador2 = 3;
int servoPin = 4;
unsigned long t_now;

int angle = 0;   // servo position in degrees
int servoLoop = 0;   // servo em modo automático
int servoLoopPosicao = 0;   // servo em modo automático
unsigned long servoLoopMicros = 0;
unsigned long printbtMicros = 0;

Servo servo;
void setup()
{
  Serial.begin(9600);
  bt.begin(9600);
  pinMode(atuador1, OUTPUT);
  pinMode(atuador2, OUTPUT);
  servo.attach(servoPin);
}
void loop()
{

  servo.write(angle);
  // Read device output if available.
  if (bt.available())
  {
    while (bt.available())
    {
      char r = (char)bt.read();
      run(r);
    }
  }

  // Read user input if available.
  if (Serial.available())
  {
    delay(10); // The DELAY!
    char r = (char)Serial.read();
    run(r);
    bt.write(r);
  }
  servoloop();
  //printbt();
}
void printbt() {
  unsigned long t_now = millis();
  if (t_now > (printbtMicros + 2000)) {
    printbtMicros = t_now;
    bt.println("OK");
  }
}

void servoloop() {
  if (servoLoop == 1) {
    unsigned long t_now = millis();
    if (t_now > (servoLoopMicros + 15)) {
      servoLoopMicros = t_now;
      if (servoLoopPosicao == 0) {
        if ( angle < 180)
        {
          angle++;
        } else {
          servoLoopPosicao = 1;
        }
      } else  if (servoLoopPosicao == 1) {
        if ( angle > 0)
        {
          angle--;
        } else {
          servoLoopPosicao = 0;
        }
      }
    }
  }
}
void run(char r) {
  command += r;
  if (r == ';' || r == ',') {
    if (command[0] == 'A' && command[1] == '1') {
      if (command[2] == '1') {
        digitalWrite(atuador1, HIGH);
      } else if (command[2] == '0') {
        digitalWrite(atuador1, LOW);
      }

    } else if (command[0] == 'A' && command[1] == '2') {
      if (command[2] == '1') {
        digitalWrite(atuador2, HIGH);
      } else if (command[2] == '0') {
        digitalWrite(atuador2, LOW);
      }
    } else  if (command[0] == 'S' && command[1] == '1') {

      if (command[2] == '5') {
        servoLoop = 1;
      } else if (command[2] == '6') {
        servoLoop = 0;
      } else {
        char buffer[4];
        buffer[0] = command[2];
        buffer[1] = command[3];
        buffer[2] = command[4];
        buffer[3] = '\0';
        angle = atoi(buffer);
        angle = angle - 100;

      }
    }
    Serial.println(command);
    bt.println(command);
    command = "";
  }
}
