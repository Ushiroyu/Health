package com.community.health.common.exception;

public interface ErrorCodes {
  int OK = 0;
  int UNAUTHORIZED = 401;
  int FORBIDDEN = 403;
  int NOT_FOUND = 404;
  int CONFLICT = 409;
  int UNPROCESSABLE = 422;
  int SERVER_ERROR = 500;

  // Appointment domain 409xx / 503xx
  int APPT_NO_SLOT = 40901;
  int APPT_DUPLICATE = 40902;
  int APPT_NOT_OPEN = 40903;
  int APPT_TEMP_UNAVAILABLE = 50301;
  int APPT_FORBIDDEN_OPERATION = 40301;
  int APPT_NOT_FOUND = 40411;

  // Activity domain 4042x / 4092x
  int ACTIVITY_NOT_FOUND = 40421;
  int ACTIVITY_FULL = 40921;
  int ACTIVITY_STARTED = 40922;
  int ACTIVITY_REG_NOT_FOUND = 40422;

  // Auth domain 4093x
  int USERNAME_EXISTS = 40931;

  // Content domain 4094x
  int CONTENT_NOT_APPROVED = 40941;
}
