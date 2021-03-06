package org.alxeg.meetings.services;

import org.alxeg.meetings.models.Meeting;
import org.alxeg.meetings.models.Room;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.misc.TraceFlags;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.schema.AppointmentSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ITraceListener;
import microsoft.exchange.webservices.data.misc.NameResolution;
import microsoft.exchange.webservices.data.misc.NameResolutionCollection;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;

public class RoomServiceEws implements BeanNameAware, RoomService {
    private static Logger LOGGER = LoggerFactory.getLogger(RoomServiceEws.class);

    @Autowired
    private MeetingAdapter meetingAdapter;

    @Value("#{appProperties}")
    private Map<String, String> appProperties;

    @Value("${exchange.url}")
    private String url;

    @Value("${exchange.trace}")
    private boolean trace;

    private String roomId;

    private String login;

    private String password;

    private String domain;

    private String name;

    private String description;

    private boolean configured;

    private Room room;


    /* (non-Javadoc)
     * @see org.alxeg.meetings.services.RoomService#isConfigured()
     */

    @Override
    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public RoomServiceEws() {
    }

    @PostConstruct
    private void initializeService() {
        url = appProperties.get("exchange.url");

        login = appProperties.get("exchange." + roomId + ".login");
        password = appProperties.get("exchange." + roomId + ".password");
        domain = appProperties.get("exchange." + roomId + ".domain");
        name = appProperties.get("exchange." + roomId + ".name");
        description = appProperties.get("exchange." + roomId + ".description");

        ExchangeService service = null;

        try {
            configured = true;
            room = getRoom();
        } catch (Throwable e) {
            LOGGER.error("Failed to login service {} ", roomId, e);
            configured = false;
        } finally {
            IOUtils.closeQuietly(service);
        }
    }

    private ExchangeService login() throws Throwable {
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(login, password, domain);
        service.setCredentials(credentials);
        service.setUrl(URI.create(url));
        if (trace) {
            service.setTraceEnabled(true);
            service.setTraceFlags(EnumSet.of(TraceFlags.EwsRequest, TraceFlags.EwsResponse));
            service.setTraceListener(new ITraceListener() {
                @Override
                public void trace(String traceType, String traceMessage) {
                    LOGGER.info("{} {}", traceType, traceMessage);
                }
            });
        }
        return service;
    }

    /* (non-Javadoc)
     * @see org.alxeg.meetings.services.RoomService#getRoom()
     */

    @Override
    public Room getRoom() {
        if (room == null) {
            // try to retrieve it from exchange
            Room result = null;

            ExchangeService service = null;
            try {
                service = login();
                NameResolutionCollection resolutionCollection = service.resolveName(login);
                if (resolutionCollection.getCount() > 0) {
                    NameResolution nameResolution = resolutionCollection.nameResolutionCollection(0);
                    if (nameResolution.getContact() != null) {
                        result = new Room()
                            .withId(roomId)
                            .withName(nameResolution.getContact().getCompleteName().toString())
                            .withDescription(description);

                    } else if (nameResolution.getMailbox() != null) {
                        result = new Room()
                            .withId(roomId)
                            .withName(nameResolution.getMailbox().getName())
                            .withDescription(description);
                    }
                }
            } catch (Throwable ex) {
                LOGGER.error("Failed to get room info {}", login);
                configured = false;
            } finally {
                IOUtils.closeQuietly(service);
            }

            if (name != null && result != null) {
                result.setName(name);
            }

            return result;

        } else {
            return room;
        }
    }


    /* (non-Javadoc)
     * @see org.alxeg.meetings.services.RoomService#getMeetings(java.util.Date, java.util.Date)
     */

    @Override
    public List<Meeting> getMeetings(Date startDate, Date endDate) {
        ExchangeService service = null;
        try {
            service = login();
            CalendarFolder cf = CalendarFolder.bind(service, WellKnownFolderName.Calendar);
            CalendarView cv = new CalendarView(startDate, endDate);
            cv.setPropertySet(new PropertySet(AppointmentSchema.Start, AppointmentSchema.End, AppointmentSchema.Subject,
                                              AppointmentSchema.Organizer, AppointmentSchema.AppointmentState,
                                              AppointmentSchema.StartTimeZone, AppointmentSchema.EndTimeZone, AppointmentSchema.TimeZone));
            FindItemsResults<Appointment> findResults = cf.findAppointments(cv);
            return meetingAdapter.meetingListFromAppointments(
                                                              findResults.getItems().stream()
                                                                  .filter(appt -> {
                                                                      try {
                                                                          return (appt.getAppointmentState().intValue() & 0x0004) == 0;
                                                                      } catch (Exception e) {
                                                                          return false;
                                                                      }
                                                                  })
                                                                  .collect(Collectors.toList()));

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        } finally {
            IOUtils.closeQuietly(service);
        }
    }


    @Override
    public void setBeanName(String name) {
        this.roomId = name;
    }


}

