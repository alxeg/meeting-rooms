package org.alxeg.meetings.controllers;

import org.alxeg.meetings.models.Meeting;
import org.alxeg.meetings.models.Room;
import org.alxeg.meetings.services.RoomService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api")
public class ApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private Map<String, RoomService> rooms;

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseEntity<ErrorResponse> handleException(Throwable e) {
        LOGGER.error("Exception occured: {}", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("rooms")
    @ResponseBody
    public List<Room> getRooms() {
        return rooms.values().stream()
                .filter(RoomService::isConfigured)
                .map(RoomService::getRoom)
                .sorted((e1, e2) -> e1.getId().compareTo(e2.getId()) )
                .collect(Collectors.toList());
    }
    
    @RequestMapping("calendar/{room}")
    @ResponseBody
    public List<Meeting> userCalendar(@PathVariable String room, @RequestParam String start, @RequestParam String end) {
        RoomService roomService = rooms.get(room);
        if (roomService!=null && roomService.isConfigured()) {
            return roomService.getMeetings(getDate(start), getDate(end));
            //return roomService.getMeetings(getStartOfDay(new Date()), getEndOfDay(new Date()));
        }
        throw new RuntimeException("Invalid room");
    }

    private Date getDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unused")
    private Date getEndOfDay(Date date) {
        return DateUtils.addMilliseconds(DateUtils.ceiling(date, Calendar.DATE), -1);
    }

    @SuppressWarnings("unused")
    private Date getStartOfDay(Date date) {
        return DateUtils.truncate(date, Calendar.DATE);
    }

}

