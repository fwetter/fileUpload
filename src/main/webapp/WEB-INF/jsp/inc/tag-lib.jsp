<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>

<%-- Per line break --%><% String newline = "\r\n"; pageContext.setAttribute("newline",newline); %>
<c:set var="URL_ICONE" scope="page" value="https://s3-eu-west-1.amazonaws.com/w4tbucket/web/commons/icons/hand-drawn/32/"/>
<c:set var="URL_FE" scope="page" value="http://bookingable.web4travel.it"/>