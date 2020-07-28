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

  /**
   * Returns collection of time slots, in which we can book the meeting.
   * @param events   All events in the day.
   * @param request  Information about event that we want to add (attendees, required duration).
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // hash map, where key is the moment of time and value is
    // the change in the number of available attendees at that particular time.
    // For example: A is the mandatory attendee.
    // Events    : ___|--A--|__
    // TimePoints: 0  1     2 3
    // Hash map looks like {0=1=0, 1=-1=0, 2=1=0, 3=-1=0}.
    // +1 for 0 and 2, because A starts the day without a meeting/finishes the meeting
    // -1 for 1 and 3, because it's the end of the day/A goes to the meeting
    HashMap<Integer, Long> timePointsOfAvailabilityChange = new HashMap<>();
    Collection<String> attendees = request.getAttendees();

    long numberOfAttendeesRequired = attendees.size();
    long requiredDuration = request.getDuration();

    // add start and end times of the day
    timePointsOfAvailabilityChange.put(0, numberOfAttendeesRequired);
    timePointsOfAvailabilityChange.put(TimeRange.WHOLE_DAY.end(), -numberOfAttendeesRequired);

    // add all time point, where the number of free attendees is changing
    for (Event event : events) {
      // the event can have attendees that are not present in the request,
      // so we shouldn't count them
      Collection<String> attendeesOfExistingEvent = new ArrayList<>(event.getAttendees());
      attendeesOfExistingEvent.retainAll(attendees);

      // if intersection isn't empty
      if (!attendeesOfExistingEvent.isEmpty()) {
        int numAttendeesOfExistingEvent = attendeesOfExistingEvent.size();
        int startTime = event.getWhen().start();
        int endTime = event.getWhen().end();

        timePointsOfAvailabilityChange.put(startTime,
                timePointsOfAvailabilityChange.getOrDefault(startTime, 0L) - numAttendeesOfExistingEvent);
        timePointsOfAvailabilityChange.put(endTime,
                timePointsOfAvailabilityChange.getOrDefault(endTime, 0L) + numAttendeesOfExistingEvent);
      }
    }

    List<Integer> sortedTimes =
            timePointsOfAvailabilityChange.keySet().stream().sorted().collect(Collectors.toList());

    long numberOfAttendeesCurrentlyAvailable = 0;
    int previousTime = 0;
    Collection<TimeRange> timeSlots = new ArrayList<>();

    // find time slots with the help of scan line method
    for (int currentTime : sortedTimes) {
      if (numberOfAttendeesCurrentlyAvailable == numberOfAttendeesRequired
              && (currentTime - previousTime) >= requiredDuration) {
        timeSlots.add(TimeRange.fromStartEnd(previousTime, currentTime, false));
      }
      numberOfAttendeesCurrentlyAvailable += timePointsOfAvailabilityChange.get(currentTime);
      previousTime = currentTime;
    }

    return timeSlots;
  }
}