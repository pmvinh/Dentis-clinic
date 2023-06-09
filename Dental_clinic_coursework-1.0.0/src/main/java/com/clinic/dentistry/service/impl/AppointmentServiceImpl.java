package com.clinic.dentistry.service.impl;

import com.clinic.dentistry.models.Appointment;
import com.clinic.dentistry.models.User;
import com.clinic.dentistry.repo.AppointmentRepository;
import com.clinic.dentistry.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public Map<User, Map<String, ArrayList<String>>> getAvailableDatesByDoctors(Iterable<User> doctors) {
        Map<User, Map<String ,ArrayList<String>>> availableDatesByDoctor = new HashMap<>();
        Map<String ,ArrayList<String>> availableDates;
        ArrayList<String> avalibleTimes;
        Boolean dateTaken;

        Iterable<Appointment> appointments = appointmentRepository.findByActiveTrue();

        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

        for (User doctor : doctors) {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusWeeks(2);
            availableDates = new TreeMap<>();
            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                if (!(date.getDayOfWeek().equals(DayOfWeek.SUNDAY) || (date.getDayOfWeek().equals(DayOfWeek.MONDAY)))) {
                    avalibleTimes = new ArrayList();
                    LocalTime workStart = LocalTime.of(8, 00);
                    LocalTime workEnd = LocalTime.of(9, 00);
                    if (doctor.getEmployee() != null) {
                        workStart = doctor.getEmployee().getWorkStart();
                        workEnd = doctor.getEmployee().getWorkEnd();
                    }
                    LocalDateTime startTime = date.atTime(workStart);
                    LocalDateTime endTime = date.atTime(workEnd);
                    for (LocalDateTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(30)) {
                        dateTaken = Boolean.FALSE;
                        for (Appointment appointment : appointments) {
                            if (appointment.getDoctor().equals(doctor.getEmployee()) && appointment.getDate().equals(time)) {
                                dateTaken = Boolean.TRUE;
                            }
                        }
                        if (dateTaken.equals(Boolean.FALSE)) {
                            avalibleTimes.add(time.format(formatterTime));
                        }
                    }
                    availableDates.put(date.format(formatterDate), avalibleTimes);
                }
            }
            availableDatesByDoctor.put(doctor, availableDates);
        }

        return availableDatesByDoctor;
    }
}
