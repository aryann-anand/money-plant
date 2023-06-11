package com.christo.moneyplant.models.ws.response;


public class ResponseBase {

        private final int status_code;
        private final String event;

        ResponseBase (int status_code, String event) {
            this.status_code = status_code;
            this.event = event;
        }

        public int getStatus_code() {
            return status_code;
        }

        public String getEvent() {
            return event;
        }

}
