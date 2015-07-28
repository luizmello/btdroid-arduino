#include <SoftwareSerial.h>
SoftwareSerial mySerial(10, 11); // TX, RX
String command = "";
// Stores response of bluetooth device
// which simply allows \n between each
// response.
/*
Comando  Resposta  Função
AT  OK  Teste de comunicação
AT+VERSION  OKlinvorV1.8  Mostra a versão do firmware
AT+NAMExyz  OKsetname   Altera o nome do módulo
AT+PIN1234  OKsetPIN  Altera a senha do módulo
AT+BAUD1  OK1200  Seta o baud rate em 1200
AT+BAUD2  OK2400  Seta o baud rate em 2400
AT+BAUD3  OK4800  Seta o baud rate em 4800
AT+BAUD4  OK9600  Seta o baud rate em 9600
AT+BAUD5  OK19200   Seta o baud rate em 19200
AT+BAUD6  OK38400   Seta o baud rate em 38400
AT+BAUD7  OK57600   Seta o baud rate em 57600
AT+BAUD8  OK115200  Seta o baud rate em 115200
AT+BAUD9  OK230400  Seta o baud rate em 230400
AT+BAUDA  OK460800  Seta o baud rate em 460800
AT+BAUDB  OK921600  Seta o baud rate em 921600
AT+BAUDC  OK1382400   Seta o baud rate em 1382400
 */
void setup()
{
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  Serial.println("Type AT commands!\nAT\nAT+VERSION\nAT+NAMELFD\nAT+PIN1234");
  // SoftwareSerial "com port" data rate. JY-MCU v1.03 defaults to 9600.
  mySerial.begin(9600);
}

void loop()
{
  // Read device output if available.
  if (mySerial.available())
  {
    while (mySerial.available())
    { // While there is more to be read, keep reading.
      command += (char)mySerial.read();
    }
    Serial.print(command);
    command = ""; // No repeats
  }

  // Read user input if available.
  if (Serial.available())
  {
    delay(10); // The DELAY!
    mySerial.write(Serial.read());
  }

}// END loop()
