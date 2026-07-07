CHANGES MADE 

URI CONSTANTS : 

    /* LifeDisabilityBandOverrideController */
    public static final String GET_LIFE_DIS_BAND_OVERRIDES = COMP_AND_EMP_REGEX_PLACEHOLDER
            + "life-dis-band-overrides";
    public static final String CREATE_LIFE_DIS_BAND_OVERRIDE = COMP_AND_EMP_REGEX_PLACEHOLDER
            + "life-dis-band-overrides";
    



LifeDisabilityBandOverrideDao

 Set<LifeDisabilityBandOverride> findByActive(boolean active);


A NEW CONTROLLER 



@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Life Disability Band Override Controller")
public class LifeDisabilityBandOverrideController {

    @Autowired
    private LifeDisabilityBandOverrideService lifeDisabilityBandOverrideService;

    @GetMapping(value = URIConstants.GET_LIFE_DIS_BAND_OVERRIDES)
    @ApiOperation(value = "Gets All Active Life Disability Band Overrides", response = LifeDisabilityBandOverrideDto.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "All active Life Disability Band Overrides retrieved successfully") })
    @ResponseBody
    public List<LifeDisabilityBandOverrideDto> getAllActiveOverrides(HttpServletRequest request) {
        return lifeDisabilityBandOverrideService.findAllActive();
    }

    @PostMapping(value = URIConstants.CREATE_LIFE_DIS_BAND_OVERRIDE)
    @ApiOperation(value = "Creates a Life Disability Band Override", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Life Disability Band Override created successfully") })
    @ResponseBody
    public void createOverride(HttpServletRequest request,
            @RequestBody final LifeDisabilityBandOverrideDto dto) {
        lifeDisabilityBandOverrideService.createOverride(dto);
    }

}


LifeDisabilityBandOverrideService

  List<LifeDisabilityBandOverrideDto> findAllActive();





RSPONSE CAME WHEN THE URL WAS TRIGGED IN THE INSOMIA 
URL : http://localhost:8089/api-bss/v1.0/benefits/001/00010164141/life-dis-band-overrides
[
	{
		"id": 20,
		"companyCode": "G48",
		"companyName": "J Comp 8891 Vend Inc.",
		"planType": null,
		"startDate": "2026-06-10",
		"endDate": "2026-12-30",
		"active": true,
		"approverId": "00002340277",
		"approverName": "Nanny Heth",
		"createTime": 1782970804653,
		"createdById": null,
		"createdByName": "Wells Tullar",
		"lastUpdatedById": null,
		"lastUpdatedByName": "Verla Elsberry",
		"exchange": null,
		"realmId": 0,
		"quarter": null,
		"lifeBand": "2",
		"disBand": "4"
	},
	{
		"id": 21,
		"companyCode": "G48",
		"companyName": "J Comp 8891 Vend Inc.",
		"planType": null,
		"startDate": "2026-06-01",
		"endDate": "2026-12-31",
		"active": true,
		"approverId": "00002340277",
		"approverName": "Nanny Heth",
		"createTime": 1782970804653,
		"createdById": null,
		"createdByName": "Wells Tullar",
		"lastUpdatedById": null,
		"lastUpdatedByName": "Verla Elsberry",
		"exchange": null,
		"realmId": 0,
		"quarter": null,
		"lifeBand": "2",
		"disBand": "4"
	},
	{
		"id": 30,
		"companyCode": "L13",
		"companyName": "M Comp 1831 Vend Inc.",
		"planType": null,
		"startDate": "2026-04-04",
		"endDate": "2026-12-31",
		"active": true,
		"approverId": "00002340277",
		"approverName": "Nanny Heth",
		"createTime": 1783327840155,
		"createdById": null,
		"createdByName": "Wells Tullar",
		"lastUpdatedById": null,
		"lastUpdatedByName": "Verla Elsberry",
		"exchange": null,
		"realmId": 0,
		"quarter": null,
		"lifeBand": "4",
		"disBand": "3"
	}
]




EXPECTED O/P: 


Response:
[
  {
    "id":                     1001,
    "companyCode":            "L13",
    "companyName":            "M Comp 1831 Vend Inc.",
    "startDate":              "2026-05-25",
    "endDate":                "2026-12-31",
    "overrideLifeBand":       "1",
    "overrideDisabilityBand": "1",
    "active":                 true,
    "approverId":             "00002340287",
    "approverName":           "Cynthea D Markarian",
    "exchange": "TriNet III",
    "quarter":                "Q2"
  },
  {
    "id":                     1002,
    "companyCode":            "X77",
    "companyName":            "Acme Corp Solutions LLC",
    "startDate":              "2026-01-01",
    "endDate":                "2026-06-30",
    "overrideLifeBand":       "1",
    "overrideDisabilityBand": "1",
    "active":                 true,
    "approverId":             "00001122334",
    "approverName":           "Robert T Williams",
    "exchange": "TriNet III",
    "quarter":                "Q2"
  }
]
