// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.*;
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  private class AvailabilityChange {
    long mandatoryAttendees;
    long optionalAttendees;

    public AvailabilityChange(long mandatoryAttendees, long optionalAttendees) {
      this.mandatoryAttendees = mandatoryAttendees;
      this.optionalAttendees = optionalAttendees;
    }
  }

  /**
   * Returns collection of time slots, in which we can book the meeting.
   * The event has mandatory and optional attendees. We should find time slots
   * when all of them will be available for the required duration of the meeting.
   * If there're no such time slots, then we should find appropriate time slots
   * for mandatory attendees only.
   * @param events   All events in the day.
   * @param request  Information about event that we want to add
   *                 (mandatory and optional attendees, required duration).
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // hash map, where key is the moment of time and value is
    // the change in the number of available attendees at that particular time.
    // For example: A is the mandatory attendee.
    // Events    : ___|--A--|__
    // TimePoints: 0  1     2 3
    // Hash map looks like {0=1=0, 1=-1=0, 2=1=0, 3=-1=0}.
    // +1 for mandatory attendees for 0 and 2, because A starts the day without a meeting/finishes the meeting
    // -1 for mandatory attendees for 1 and 3, because it's the end of the day/A goes to the meeting
    HashMap<Integer, AvailabilityChange> timePointsOfAvailabilityChange = new HashMap<>();
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    long numOfMandAttendeesRequired = mandatoryAttendees.size();
    long numOfOptAttendeesRequired = optionalAttendees.size();
    long numOfAttendeesRequired = numOfMandAttendeesRequired + numOfOptAttendeesRequired;
    long requiredDuration = request.getDuration();

    // add start and end times of the day
    timePointsOfAvailabilityChange.put(0,
            new AvailabilityChange(numOfMandAttendeesRequired,
                    numOfOptAttendeesRequired));
    timePointsOfAvailabilityChange.put(TimeRange.WHOLE_DAY.end(),
            new AvailabilityChange(-numOfMandAttendeesRequired,
                    -numOfOptAttendeesRequired));

    // add all time point, where the number of free attendees is changing
    for (Event existingEvent : events) {
      // the event can have attendees that are not present in the request,
      // so we shouldn't count them
      Collection<String> mandAttendeesOfExistingEvent = new ArrayList<>(existingEvent.getAttendees());
      mandAttendeesOfExistingEvent.retainAll(mandatoryAttendees);
      Collection<String> optAttendeesOfExistingEvent = new ArrayList<>(existingEvent.getAttendees());
      optAttendeesOfExistingEvent.retainAll(optionalAttendees);

      // if intersection isn't empty, then it's a time moment, where the available
      // number of attendees changes, in other case, we skip this event
      if (mandAttendeesOfExistingEvent.isEmpty() && optAttendeesOfExistingEvent.isEmpty()) {
        continue;
      }

      AvailabilityChange timePointChanges;
      long changeOfMandAttendees, changeOfOptAttendees;
      int eventStartTime = existingEvent.getWhen().start();
      int eventEndTime = existingEvent.getWhen().end();

      int mandAttendeesOfExistingEventSize = mandAttendeesOfExistingEvent.size();
      int optAttendeesOfExistingEventSize = optAttendeesOfExistingEvent.size();

      timePointChanges = timePointsOfAvailabilityChange.getOrDefault(eventStartTime,
              new AvailabilityChange(0L, 0L));
      changeOfMandAttendees = timePointChanges.mandatoryAttendees;
      changeOfOptAttendees = timePointChanges.optionalAttendees;
      timePointsOfAvailabilityChange.put(eventStartTime, new AvailabilityChange(
              changeOfMandAttendees - mandAttendeesOfExistingEvent.size(),
              changeOfOptAttendees - optAttendeesOfExistingEvent.size()));

      timePointChanges = timePointsOfAvailabilityChange.getOrDefault(eventEndTime,
              new AvailabilityChange(0L, 0L));
      changeOfMandAttendees = timePointChanges.mandatoryAttendees;
      changeOfOptAttendees = timePointChanges.optionalAttendees;
      timePointsOfAvailabilityChange.put(eventEndTime, new AvailabilityChange(
              changeOfMandAttendees + mandAttendeesOfExistingEventSize,
              changeOfOptAttendees + optAttendeesOfExistingEventSize));
    }

    List<Integer> sortedTimes =
            timePointsOfAvailabilityChange.keySet().stream().sorted().collect(Collectors.toList());

    long numberOfMandatoryAttendeesCurrentlyAvailable = 0;
    long numberOfOptionalAttendeesCurrentlyAvailable = 0;
    long numberOfAttendeesCurrentlyAvailable = 0;
    int previousTimeForAll = 0;
    int previousTimeForMandAttendees = 0;
    Collection<TimeRange> timeSlotsForAll = new ArrayList<>();
    Collection<TimeRange> timeSlotsForMandatory = new ArrayList<>();

    // find time slots with the help of scan line method
    for (int currentTime : sortedTimes) {
      long changeOfMandAttendees = timePointsOfAvailabilityChange.get(currentTime).mandatoryAttendees;
      long changeOfOptAttendees = timePointsOfAvailabilityChange.get(currentTime).optionalAttendees;
      numberOfAttendeesCurrentlyAvailable =
              numberOfMandatoryAttendeesCurrentlyAvailable + numberOfOptionalAttendeesCurrentlyAvailable;

      // add new time slot for mandatory attendees only (we should ignore time points,
      // where changes only the number of optional attendees available,
      // so we want need to merge neighboring time slots, e.g. [0, 10) and [10, 20))
      if (numberOfMandatoryAttendeesCurrentlyAvailable == numOfMandAttendeesRequired
              && changeOfMandAttendees != 0
              && (currentTime - previousTimeForMandAttendees) >= requiredDuration) {
        timeSlotsForMandatory.add(TimeRange.fromStartEnd(previousTimeForMandAttendees, currentTime, false));
      }

      // add new time slot for both mandatory and optional attendees
      if (numberOfAttendeesCurrentlyAvailable == numOfAttendeesRequired
              && (currentTime - previousTimeForAll) >= requiredDuration) {
        timeSlotsForAll.add(TimeRange.fromStartEnd(previousTimeForAll, currentTime, false));
      }

      numberOfMandatoryAttendeesCurrentlyAvailable += changeOfMandAttendees;
      numberOfOptionalAttendeesCurrentlyAvailable += changeOfOptAttendees;
      previousTimeForAll = currentTime;

      if (changeOfMandAttendees != 0)
        previousTimeForMandAttendees = currentTime;
    }

    return timeSlotsForAll.isEmpty() ? timeSlotsForMandatory : timeSlotsForAll;
  }
}