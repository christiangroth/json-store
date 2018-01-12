package de.chrgroth.jsonstore.json.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllFieldsTestEntity {

    private byte byt;
    private byte[] byts;
    private Byte bytWrapper;
    private Byte[] bytWrappers;
    private Set<Byte> bytWrappersSet;
    private List<Byte> bytWrappersList;
    private Map<String, Byte> bytWrappersMap;

    private short shrt;
    private short[] shrts;
    private Short shrtWrapper;
    private Short[] shrtWrappers;
    private Set<Short> shrtWrappersSet;
    private List<Short> shrtWrappersList;
    private Map<String, Short> shrtWrappersMap;

    private int integer;
    private int[] integers;
    private Integer integerWrapper;
    private Integer[] integerWrappers;
    private Set<Integer> integerWrappersSet;
    private List<Integer> integerWrappersList;
    private Map<String, Integer> integerWrappersMap;

    private long lng;
    private long[] lngs;
    private Long lngWrapper;
    private Long[] lngWrappers;
    private Set<Long> lngWrappersSet;
    private List<Long> lngWrappersList;
    private Map<String, Long> lngWrappersMap;

    private float flt;
    private float[] flts;
    private Float fltWrapper;
    private Float[] fltWrappers;
    private Set<Float> fltWrappersSet;
    private List<Float> fltWrappersList;
    private Map<String, Float> fltWrappersMap;

    private double dbl;
    private double[] dbls;
    private Double dblWrapper;
    private Double[] dblWrappers;
    private Set<Double> dblWrappersSet;
    private List<Double> dblWrappersList;
    private Map<String, Double> dblWrappersMap;

    private boolean bool;
    private boolean[] bools;
    private Boolean boolWrapper;
    private Boolean[] boolWrappers;
    private Set<Boolean> boolWrappersSet;
    private List<Boolean> boolWrappersList;
    private Map<String, Boolean> boolWrappersMap;

    private char chr;
    private char[] chrs;
    private Character chrWrapper;
    private Character[] chrWrappers;
    private Set<Character> chrWrappersSet;
    private List<Character> chrWrappersList;
    private Map<String, Character> chrWrappersMap;

    private String str;
    private String[] strs;
    private Set<String> strsSet;
    private List<String> strsList;
    private Map<String, String> strsMap;

    private Date date;
    private Date[] dates;
    private Set<Date> datesSet;
    private List<Date> datesList;
    private Map<String, Date> datesMap;

    private LocalDateTime ldt;
    private LocalDateTime[] ldts;
    private Set<LocalDateTime> ldtsSet;
    private List<LocalDateTime> ldtsList;
    private Map<String, LocalDateTime> ldtsMap;

    private ReferencedChildEntity ref;
    private ReferencedChildEntity[] refs;
    private Set<ReferencedChildEntity> refsSet;
    private List<ReferencedChildEntity> refsList;
    private Map<String, ReferencedChildEntity> refsMap;

    public byte getByt() {
        return byt;
    }

    public void setByt(byte byt) {
        this.byt = byt;
    }

    public byte[] getByts() {
        return byts;
    }

    public void setByts(byte[] byts) {
        this.byts = byts;
    }

    public Byte getBytWrapper() {
        return bytWrapper;
    }

    public void setBytWrapper(Byte bytWrapper) {
        this.bytWrapper = bytWrapper;
    }

    public Byte[] getBytWrappers() {
        return bytWrappers;
    }

    public void setBytWrappers(Byte[] bytWrappers) {
        this.bytWrappers = bytWrappers;
    }

    public Set<Byte> getBytWrappersSet() {
        return bytWrappersSet;
    }

    public void setBytWrappersSet(Set<Byte> bytWrappersSet) {
        this.bytWrappersSet = bytWrappersSet;
    }

    public List<Byte> getBytWrappersList() {
        return bytWrappersList;
    }

    public void setBytWrappersList(List<Byte> bytWrappersList) {
        this.bytWrappersList = bytWrappersList;
    }

    public Map<String, Byte> getBytWrappersMap() {
        return bytWrappersMap;
    }

    public void setBytWrappersMap(Map<String, Byte> bytWrappersMap) {
        this.bytWrappersMap = bytWrappersMap;
    }

    public short getShrt() {
        return shrt;
    }

    public void setShrt(short shrt) {
        this.shrt = shrt;
    }

    public short[] getShrts() {
        return shrts;
    }

    public void setShrts(short[] shrts) {
        this.shrts = shrts;
    }

    public Short getShrtWrapper() {
        return shrtWrapper;
    }

    public void setShrtWrapper(Short shrtWrapper) {
        this.shrtWrapper = shrtWrapper;
    }

    public Short[] getShrtWrappers() {
        return shrtWrappers;
    }

    public void setShrtWrappers(Short[] shrtWrappers) {
        this.shrtWrappers = shrtWrappers;
    }

    public Set<Short> getShrtWrappersSet() {
        return shrtWrappersSet;
    }

    public void setShrtWrappersSet(Set<Short> shrtWrappersSet) {
        this.shrtWrappersSet = shrtWrappersSet;
    }

    public List<Short> getShrtWrappersList() {
        return shrtWrappersList;
    }

    public void setShrtWrappersList(List<Short> shrtWrappersList) {
        this.shrtWrappersList = shrtWrappersList;
    }

    public Map<String, Short> getShrtWrappersMap() {
        return shrtWrappersMap;
    }

    public void setShrtWrappersMap(Map<String, Short> shrtWrappersMap) {
        this.shrtWrappersMap = shrtWrappersMap;
    }

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public int[] getIntegers() {
        return integers;
    }

    public void setIntegers(int[] integers) {
        this.integers = integers;
    }

    public Integer getIntegerWrapper() {
        return integerWrapper;
    }

    public void setIntegerWrapper(Integer integerWrapper) {
        this.integerWrapper = integerWrapper;
    }

    public Integer[] getIntegerWrappers() {
        return integerWrappers;
    }

    public void setIntegerWrappers(Integer[] integerWrappers) {
        this.integerWrappers = integerWrappers;
    }

    public Set<Integer> getIntegerWrappersSet() {
        return integerWrappersSet;
    }

    public void setIntegerWrappersSet(Set<Integer> integerWrappersSet) {
        this.integerWrappersSet = integerWrappersSet;
    }

    public List<Integer> getIntegerWrappersList() {
        return integerWrappersList;
    }

    public void setIntegerWrappersList(List<Integer> integerWrappersList) {
        this.integerWrappersList = integerWrappersList;
    }

    public Map<String, Integer> getIntegerWrappersMap() {
        return integerWrappersMap;
    }

    public void setIntegerWrappersMap(Map<String, Integer> integerWrappersMap) {
        this.integerWrappersMap = integerWrappersMap;
    }

    public long getLng() {
        return lng;
    }

    public void setLng(long lng) {
        this.lng = lng;
    }

    public long[] getLngs() {
        return lngs;
    }

    public void setLngs(long[] lngs) {
        this.lngs = lngs;
    }

    public Long getLngWrapper() {
        return lngWrapper;
    }

    public void setLngWrapper(Long lngWrapper) {
        this.lngWrapper = lngWrapper;
    }

    public Long[] getLngWrappers() {
        return lngWrappers;
    }

    public void setLngWrappers(Long[] lngWrappers) {
        this.lngWrappers = lngWrappers;
    }

    public Set<Long> getLngWrappersSet() {
        return lngWrappersSet;
    }

    public void setLngWrappersSet(Set<Long> lngWrappersSet) {
        this.lngWrappersSet = lngWrappersSet;
    }

    public List<Long> getLngWrappersList() {
        return lngWrappersList;
    }

    public void setLngWrappersList(List<Long> lngWrappersList) {
        this.lngWrappersList = lngWrappersList;
    }

    public Map<String, Long> getLngWrappersMap() {
        return lngWrappersMap;
    }

    public void setLngWrappersMap(Map<String, Long> lngWrappersMap) {
        this.lngWrappersMap = lngWrappersMap;
    }

    public float getFlt() {
        return flt;
    }

    public void setFlt(float flt) {
        this.flt = flt;
    }

    public float[] getFlts() {
        return flts;
    }

    public void setFlts(float[] flts) {
        this.flts = flts;
    }

    public Float getFltWrapper() {
        return fltWrapper;
    }

    public void setFltWrapper(Float fltWrapper) {
        this.fltWrapper = fltWrapper;
    }

    public Float[] getFltWrappers() {
        return fltWrappers;
    }

    public void setFltWrappers(Float[] fltWrappers) {
        this.fltWrappers = fltWrappers;
    }

    public Set<Float> getFltWrappersSet() {
        return fltWrappersSet;
    }

    public void setFltWrappersSet(Set<Float> fltWrappersSet) {
        this.fltWrappersSet = fltWrappersSet;
    }

    public List<Float> getFltWrappersList() {
        return fltWrappersList;
    }

    public void setFltWrappersList(List<Float> fltWrappersList) {
        this.fltWrappersList = fltWrappersList;
    }

    public Map<String, Float> getFltWrappersMap() {
        return fltWrappersMap;
    }

    public void setFltWrappersMap(Map<String, Float> fltWrappersMap) {
        this.fltWrappersMap = fltWrappersMap;
    }

    public double getDbl() {
        return dbl;
    }

    public void setDbl(double dbl) {
        this.dbl = dbl;
    }

    public double[] getDbls() {
        return dbls;
    }

    public void setDbls(double[] dbls) {
        this.dbls = dbls;
    }

    public Double getDblWrapper() {
        return dblWrapper;
    }

    public void setDblWrapper(Double dblWrapper) {
        this.dblWrapper = dblWrapper;
    }

    public Double[] getDblWrappers() {
        return dblWrappers;
    }

    public void setDblWrappers(Double[] dblWrappers) {
        this.dblWrappers = dblWrappers;
    }

    public Set<Double> getDblWrappersSet() {
        return dblWrappersSet;
    }

    public void setDblWrappersSet(Set<Double> dblWrappersSet) {
        this.dblWrappersSet = dblWrappersSet;
    }

    public List<Double> getDblWrappersList() {
        return dblWrappersList;
    }

    public void setDblWrappersList(List<Double> dblWrappersList) {
        this.dblWrappersList = dblWrappersList;
    }

    public Map<String, Double> getDblWrappersMap() {
        return dblWrappersMap;
    }

    public void setDblWrappersMap(Map<String, Double> dblWrappersMap) {
        this.dblWrappersMap = dblWrappersMap;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public boolean[] getBools() {
        return bools;
    }

    public void setBools(boolean[] bools) {
        this.bools = bools;
    }

    public Boolean getBoolWrapper() {
        return boolWrapper;
    }

    public void setBoolWrapper(Boolean boolWrapper) {
        this.boolWrapper = boolWrapper;
    }

    public Boolean[] getBoolWrappers() {
        return boolWrappers;
    }

    public void setBoolWrappers(Boolean[] boolWrappers) {
        this.boolWrappers = boolWrappers;
    }

    public Set<Boolean> getBoolWrappersSet() {
        return boolWrappersSet;
    }

    public void setBoolWrappersSet(Set<Boolean> boolWrappersSet) {
        this.boolWrappersSet = boolWrappersSet;
    }

    public List<Boolean> getBoolWrappersList() {
        return boolWrappersList;
    }

    public void setBoolWrappersList(List<Boolean> boolWrappersList) {
        this.boolWrappersList = boolWrappersList;
    }

    public Map<String, Boolean> getBoolWrappersMap() {
        return boolWrappersMap;
    }

    public void setBoolWrappersMap(Map<String, Boolean> boolWrappersMap) {
        this.boolWrappersMap = boolWrappersMap;
    }

    public char getChr() {
        return chr;
    }

    public void setChr(char chr) {
        this.chr = chr;
    }

    public char[] getChrs() {
        return chrs;
    }

    public void setChrs(char[] chrs) {
        this.chrs = chrs;
    }

    public Character getChrWrapper() {
        return chrWrapper;
    }

    public void setChrWrapper(Character chrWrapper) {
        this.chrWrapper = chrWrapper;
    }

    public Character[] getChrWrappers() {
        return chrWrappers;
    }

    public void setChrWrappers(Character[] chrWrappers) {
        this.chrWrappers = chrWrappers;
    }

    public Set<Character> getChrWrappersSet() {
        return chrWrappersSet;
    }

    public void setChrWrappersSet(Set<Character> chrWrappersSet) {
        this.chrWrappersSet = chrWrappersSet;
    }

    public List<Character> getChrWrappersList() {
        return chrWrappersList;
    }

    public void setChrWrappersList(List<Character> chrWrappersList) {
        this.chrWrappersList = chrWrappersList;
    }

    public Map<String, Character> getChrWrappersMap() {
        return chrWrappersMap;
    }

    public void setChrWrappersMap(Map<String, Character> chrWrappersMap) {
        this.chrWrappersMap = chrWrappersMap;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String[] getStrs() {
        return strs;
    }

    public void setStrs(String[] strs) {
        this.strs = strs;
    }

    public Set<String> getStrsSet() {
        return strsSet;
    }

    public void setStrsSet(Set<String> strsSet) {
        this.strsSet = strsSet;
    }

    public List<String> getStrsList() {
        return strsList;
    }

    public void setStrsList(List<String> strsList) {
        this.strsList = strsList;
    }

    public Map<String, String> getStrsMap() {
        return strsMap;
    }

    public void setStrsMap(Map<String, String> strsMap) {
        this.strsMap = strsMap;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date[] getDates() {
        return dates;
    }

    public void setDates(Date[] dates) {
        this.dates = dates;
    }

    public Set<Date> getDatesSet() {
        return datesSet;
    }

    public void setDatesSet(Set<Date> datesSet) {
        this.datesSet = datesSet;
    }

    public List<Date> getDatesList() {
        return datesList;
    }

    public void setDatesList(List<Date> datesList) {
        this.datesList = datesList;
    }

    public Map<String, Date> getDatesMap() {
        return datesMap;
    }

    public void setDatesMap(Map<String, Date> datesMap) {
        this.datesMap = datesMap;
    }

    public LocalDateTime getLdt() {
        return ldt;
    }

    public void setLdt(LocalDateTime ldt) {
        this.ldt = ldt;
    }

    public LocalDateTime[] getLdts() {
        return ldts;
    }

    public void setLdts(LocalDateTime[] ldts) {
        this.ldts = ldts;
    }

    public Set<LocalDateTime> getLdtsSet() {
        return ldtsSet;
    }

    public void setLdtsSet(Set<LocalDateTime> ldtsSet) {
        this.ldtsSet = ldtsSet;
    }

    public List<LocalDateTime> getLdtsList() {
        return ldtsList;
    }

    public void setLdtsList(List<LocalDateTime> ldtsList) {
        this.ldtsList = ldtsList;
    }

    public Map<String, LocalDateTime> getLdtsMap() {
        return ldtsMap;
    }

    public void setLdtsMap(Map<String, LocalDateTime> ldtsMap) {
        this.ldtsMap = ldtsMap;
    }

    public ReferencedChildEntity getRef() {
        return ref;
    }

    public void setRef(ReferencedChildEntity ref) {
        this.ref = ref;
    }

    public ReferencedChildEntity[] getRefs() {
        return refs;
    }

    public void setRefs(ReferencedChildEntity[] refs) {
        this.refs = refs;
    }

    public Set<ReferencedChildEntity> getRefsSet() {
        return refsSet;
    }

    public void setRefsSet(Set<ReferencedChildEntity> refsSet) {
        this.refsSet = refsSet;
    }

    public List<ReferencedChildEntity> getRefsList() {
        return refsList;
    }

    public void setRefsList(List<ReferencedChildEntity> refsList) {
        this.refsList = refsList;
    }

    public Map<String, ReferencedChildEntity> getRefsMap() {
        return refsMap;
    }

    public void setRefsMap(Map<String, ReferencedChildEntity> refsMap) {
        this.refsMap = refsMap;
    }
}
