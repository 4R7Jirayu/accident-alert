const int buttonPin = 5; 
int buttonState = LOW;

void setup(){
  pinMode(buttonPin, INPUT);
  Serial.begin(9600);  
}

void loop(){
buttonState = digitalRead(buttonPin);

  if (buttonState == HIGH) {
 Serial.println(1, DEC); 
    Serial.write(0x0A);
    Serial.write(0x0D);
    buttonState == LOW
  } 
  else {
Serial.println(0, DEC); 
    Serial.write(0x0A);
    Serial.write(0x0D);
  }
} 
   delay(1000);
}
