package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.IllegalFormatPrecisionException;

public class FlightStatisticsGson {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Не передан ни один файл");
            return;
        }

        String pathToJsonFile = args[0];

        try {
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(new FileReader(pathToJsonFile), JsonObject.class);
            JsonArray ticketsArray = rootObject.getAsJsonArray("tickets");

            int totalFlightTime = 0;
            int[] flightTimes = new int[ticketsArray.size()];
            for (int i = 0; i < ticketsArray.size(); i++) {
                JsonObject ticketObject = ticketsArray.get(i).getAsJsonObject();
                if (ticketObject == null) {
                    continue;
                }
                String origin = ticketObject.get("origin").getAsString();
                String destination = ticketObject.get("destination").getAsString();

                String departureDate = ticketObject.get("departure_date").getAsString();
                String departureTime = ticketObject.get("departure_time").getAsString();
                LocalDateTime departureDateTime = LocalDateTime.parse(departureDate + " " + departureTime, DateTimeFormatter.ofPattern("dd.MM.yy H:mm"));

                String arrivalDate = ticketObject.get("arrival_date").getAsString();
                String arrivalTime = ticketObject.get("arrival_time").getAsString();
                LocalDateTime arrivalDateTime = LocalDateTime.parse(arrivalDate + " " + arrivalTime, DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));

                int flightTime = (int) Duration.between(departureDateTime, arrivalDateTime).toMinutes();
                if (origin.equals("VVO") && destination.equals("TLV")) {
                    totalFlightTime += flightTime;
                    flightTimes[i] = flightTime;
                }
            }

            int avgFlightTime = totalFlightTime / flightTimes.length;
            System.out.printf("Среднее время полета между городами Владивосток и Тель-Авив: %.2d мин.\n", avgFlightTime);

            Arrays.sort(flightTimes);
            int percentileIndex = (int) Math.ceil(0.9 * flightTimes.length);
            int percentileFlightTime = flightTimes[percentileIndex - 1];
            System.out.printf("90-й процентиль времени полета между городами Владивосток и Тель-Авив: %d мин.\n", percentileFlightTime);

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (IllegalFormatPrecisionException e){
            System.out.println("Файл поврежден. В файле отсутствует какая-либо строчка данных");
        }
    }
}
