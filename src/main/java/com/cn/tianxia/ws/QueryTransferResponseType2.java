/**
 * QueryTransferResponseType2.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.cn.tianxia.ws;

public class QueryTransferResponseType2  implements java.io.Serializable {
    private com.cn.tianxia.ws.QueryTransferResponse queryTransferResult;

    public QueryTransferResponseType2() {
    }

    public QueryTransferResponseType2(
           com.cn.tianxia.ws.QueryTransferResponse queryTransferResult) {
           this.queryTransferResult = queryTransferResult;
    }


    /**
     * Gets the queryTransferResult value for this QueryTransferResponseType2.
     * 
     * @return queryTransferResult
     */
    public com.cn.tianxia.ws.QueryTransferResponse getQueryTransferResult() {
        return queryTransferResult;
    }


    /**
     * Sets the queryTransferResult value for this QueryTransferResponseType2.
     * 
     * @param queryTransferResult
     */
    public void setQueryTransferResult(com.cn.tianxia.ws.QueryTransferResponse queryTransferResult) {
        this.queryTransferResult = queryTransferResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof QueryTransferResponseType2)) return false;
        QueryTransferResponseType2 other = (QueryTransferResponseType2) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.queryTransferResult==null && other.getQueryTransferResult()==null) || 
             (this.queryTransferResult!=null &&
              this.queryTransferResult.equals(other.getQueryTransferResult())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getQueryTransferResult() != null) {
            _hashCode += getQueryTransferResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(QueryTransferResponseType2.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ws.oxypite.com/", ">QueryTransferResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("queryTransferResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ws.oxypite.com/", "QueryTransferResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ws.oxypite.com/", "QueryTransferResponse"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
