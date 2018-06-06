#define SHIFT_DATA 2
#define SHIFT_CLK 4
#define SHIFT_LATCH 13
#define DL0 5
#define DL7 12
#define WRITE_ENABLE 3

#define SPLIT ","

boolean init_ = false;
int ee_delay = 0;
int addresses = 0;
boolean lr = true; // bits left to right

String serialData = "";
boolean fullData = false;
String splitData[5];

void processData(String id, String* data){
  if (id == "init"){                                       // Init
    addresses = data[0].toInt();
    ee_delay = data[1].toInt();
    lr = data[2].toInt() == 0;
    init_ = true;
    endFunc();
  } else if(id == "wbc"){                                   //Write byte, clear address afterwards
    checkInit();
    writeData(data[0].toInt(), data[1].toInt(), false);
    endFunc();
  } else if(id == "wbm"){                                   //Write byte, other write requests will follow
    checkInit();
    writeData(data[0].toInt(), data[1].toInt(), true);
    endFunc();
  } else if(id == "rbc"){                                    //Read byte, clear address afterwards
    checkInit();
    Serial.println(readData(data[0].toInt(), false));
  } else if(id == "rbm"){                                    //Read byte, other read requests will follow
    checkInit();
    Serial.println(readData(data[0].toInt(), false));
  } else if (id == "ra"){                                   //Read all, print all contents to serial
    checkInit();
    printContents();
    endFunc();
  } else if (id == "rp"){                                   //Read part, print contents to serial
    checkInit();
    printContents(data[0].toInt(), data[1].toInt(), data[2].toInt());
    endFunc();
  } else if (id == "clr"){                                 //Clear entire chip
    checkInit();
    clearChip();
    endFunc();
  } else if (id == "cio"){                                 //Clear IO ports
    checkInit();
    resetIO();
    endFunc();
  } else if (id == "ping"){                                 //Ping, Pong!
    endFunc();
  } else if (id == "exit"){                                 //Stop
    init_ = false;
  } else {
    Serial.println("thr,Unknown command: "+id);
  }
}

void postLoad(){
  //clearChip();
  //writeData(0, 128, false);
  //Serial.println(readData(0, false));
  
  //writeData(1, 9);
  //printContents();
  //printContents(0, 8, 500);
  //printContents(2048-2, 2048, 500);
}

void setAddress(int address, boolean output) {
  shiftOut(SHIFT_DATA, SHIFT_CLK, MSBFIRST, (address >> 8) | (output ? 0x00 : 0x80));
  shiftOut(SHIFT_DATA, SHIFT_CLK, MSBFIRST, address);

  digitalWrite(SHIFT_LATCH, LOW);
  digitalWrite(SHIFT_LATCH, HIGH);
  digitalWrite(SHIFT_LATCH, LOW);
  //delayMicroseconds(1); //Data setup time is always < 100ns
  //^ Slow CPU solves this issue
}

byte readData(int address, boolean multi){
  setAddress(address, true);
  for(int i = DL0; i <= DL7; i++){
    pinMode(i, INPUT);
  }
  delay(1);
  byte d = 0;
  if (lr){
    for(int p = DL0; p <= DL7; p++){
      d = (d << 1) + digitalRead(p);
    }
  } else {
    for(int p = DL7; p >= DL0; p--){
      d = (d << 1) + digitalRead(p);
    }
  }
  if (!multi){
    resetIO();
  }
  return d;
}

void writeData(int address, byte data, boolean multi){
  setAddress(address, false);
  for(int i = DL0; i <= DL7; i++){
    pinMode(i, OUTPUT);
  }
  if (lr){
    for (int i = DL7; i >= DL0; i--){
      digitalWrite(i, data & 1);
      data = data >> 1;
    }
  } else {
    for (int i = DL0; i <= DL7; i++){
      digitalWrite(i, data & 1);
      data = data >> 1;
    }
  }
  digitalWrite(WRITE_ENABLE, 1);
  delayMicroseconds(1);
  digitalWrite(WRITE_ENABLE, 0);
  delayMicroseconds(10);
  if (!multi) {
    resetIO();
  }
}

void resetIO(){
  setAddress(0, false);
  for (int i = DL0; i <= DL7; i++){
    digitalWrite(i, 0);
    pinMode(i, INPUT);
  }
}

void clearChip(){
  for(int i = 0; i < addresses; i++){
    delay(ee_delay);//Short wait for EEPROM
    writeData(i, 0, i != (addresses - 1));
  }
}

void printContents() {
  printContents(0, addresses, 0);
}

void printContents(int start, int end, int delay_) {
  if (start >= addresses){
    start -= addresses;
  }
  if (end >= addresses){
    end = addresses;
  }
  start -= start % 16;
  for (int base = start; base < end; base += 16) {
    byte data[16];
    for (int offset = 0; offset <= 15; offset += 1) {
      data[offset] = readData(base + offset, true);
      delay(delay_);
    }

    char buf[65];
    sprintf(buf, "%03x:  %02x %02x %02x %02x %02x %02x %02x %02x   %02x %02x %02x %02x %02x %02x %02x %02x",
            base, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7],
            data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15]);

    Serial.println(buf);
  }
  setAddress(0, false);
}

void setup() {
  digitalWrite(WRITE_ENABLE, 0);
  pinMode(WRITE_ENABLE, OUTPUT);
  pinMode(SHIFT_DATA, OUTPUT);
  pinMode(SHIFT_CLK, OUTPUT);
  pinMode(SHIFT_LATCH, OUTPUT);
  for(int i = DL0; i <= DL7; i++){
    pinMode(i, INPUT);
  }

  Serial.begin(57600);
  while (!Serial) {
    ; // Wait for serial port to connect
  }
  
  delay(10);//Wait for EEPROM
  postLoad();
}

void serialEvent(){
  while (Serial.available() && !fullData) { //Wait for data to be read
    char c = (char)Serial.read();
    if (c == '\n') {
      fullData = true;
    } else {
      serialData += c;
    }
  }
}

void loop() {
  if (fullData){
    byte b = splitStr(serialData, SPLIT);
    String data[b - 1];
    for(int i = 1; i < b; i++){
      data[i - 1] = splitData[i];
    }
    processData(splitData[0], data);
    serialData = "";
    fullData = false;
  }
}

void checkInit(){
  if (!init_){
    Serial.println("thr,Programmer not properly initialized!");
  }
}

void endFunc(){
  Serial.println("end");
}

byte splitStr(String s, String split){
  int offsdet = 0;
  int val = 0;
  int ln = s.length();
  for (int j = 0; j < ln; j++) {
    int i = j - offsdet;
    if (s.substring(i, i+1) == split) {
      splitData[val] = s.substring(0, i);
      offsdet += s.substring(0, i).length();
      s = s.substring(i + 1);
      val++;
    }
  }
  splitData[val] = s;
  return val + 1;
}
