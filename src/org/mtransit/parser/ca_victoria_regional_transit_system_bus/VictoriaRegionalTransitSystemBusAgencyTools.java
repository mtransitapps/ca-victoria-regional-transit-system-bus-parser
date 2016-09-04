package org.mtransit.parser.ca_victoria_regional_transit_system_bus;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// http://bctransit.com/*/footer/open-data
// http://bctransit.com/servlet/bctransit/data/GTFS.zip
// http://bct2.baremetal.com:8080/GoogleTransit/BCTransit/google_transit.zip
public class VictoriaRegionalTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-victoria-regional-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new VictoriaRegionalTransitSystemBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Victoria Regional Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Victoria Regional Transit System bus data... DONE in %s.\n",
				Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	private static final String INCLUDE_ONLY_SERVICE_ID_STARTS_WITH = null;
	private static final String INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 = null;

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gCalendar.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gCalendar.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gCalendarDates.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gCalendarDates.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "1"; // Victoria Regional Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gTrip.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gTrip.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			return AGENCY_COLOR_BLUE;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String DOWNTOWN = "Downtown";
	private static final String OAK_BAY = "Oak Bay";
	private static final String BEACON_HILL = "Beacon Hl";
	private static final String EXCH = "Exch";
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String ROYAL_OAK_EXCH = ROYAL_OAK + " " + EXCH;
	private static final String CAMOSUN = "Camosun";
	private static final String ROYAL_ROADS = "Royal Rds";
	private static final String JAMES_BAY = "James Bay";
	private static final String MAJESTIC = "Majestic";
	private static final String DOCKYARD = "Dockyard";
	private static final String HILLSIDE_MALL = "Hillside Mall";
	private static final String U_VIC = "UVic";
	private static final String SONGHEES = "Songhees";
	private static final String BRENTWOOD = "Brentwood";
	private static final String SAANICHTON = "Saanichton";
	private static final String SAANICHTON_EXCH = SAANICHTON + " " + EXCH;
	private static final String SWARTZ_BAY = "Swartz Bay";
	private static final String LANGFORD = "Langford";
	private static final String THETIS_HTS = "Thetis Hts";
	private static final String COLWOOD_EXCH = "Colwood " + EXCH;
	private static final String HAPPY_VLY = "Happy Vly";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		if (mTrip.getRouteId() == 2l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 3l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(BEACON_HILL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 6l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 7l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 8l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Interurban", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(SONGHEES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 11l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Vic General", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(CAMOSUN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(CAMOSUN, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HILLSIDE_MALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Admirals Walk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 25l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Admirals Walk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOCKYARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 27l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 28l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(MAJESTIC, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 30l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 31l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 39l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(ROYAL_ROADS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(LANGFORD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(COLWOOD_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Bear Mtn", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55l) {
			// TODO split ?
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HAPPY_VLY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 57l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(THETIS_HTS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 61l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Sooke", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 64l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("E Sooke", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SWARTZ_BAY, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 75l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(SAANICHTON_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 81l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(BRENTWOOD, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(SWARTZ_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 83l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merges %s & %s!s\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W){1}(exchange)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCH + "$4";

	private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("(^[\\d]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("( express.*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_VIA = Pattern.compile("( via .*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.* to )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_NUMBER.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);


	private static final Pattern UVIC = Pattern.compile("((^|\\W){1}(uvic)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String UVIC_REPLACEMENT = "$2" + U_VIC + "$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = UVIC.matcher(gStopName).replaceAll(UVIC_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
