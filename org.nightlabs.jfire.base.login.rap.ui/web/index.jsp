<%@ page language="java" %>

<html>
<style>
	font.font {
		font-family: sans-serif;
		font-size: small;
		font-weight: bold;
	}
</style>
	<head>
		<title>JFire Login</title>
	</head>
	<body>
	<center>
	<table cellpadding=2 cellspacing=0 border=0>
	<tr><td bgcolor="black"><table cellpadding=0 cellspacing=0 border=0 width=100%>
	<tr><td bgcolor="orange" align=center style="padding:2;padding-bottom:4"><b><font size=-1 color="white" face="verdana,arial"><b>JFire Login</b></font></th></tr>
	<tr><td bgcolor="white" style="padding:5"><br>
		<form action="login" method="post">
			<table>
				<% 
					String message = request.getParameter("message");
					if(message != null) 
					{
				%>
					<tr>
						<td colspan="2" align="left" ><font color="red"><%= message%></font></td>
					</tr>
				<%
					}
				%>
			
				<tr>
					<td><font class="font">User:</font></td>
					<td><input name="user" value="admin"><font></font></td>
				</tr>
				
				<tr>
					<td><font class="font">Password:</font></td>
					<td><input name="password" type="password" value="test"></td>
				</tr>
				
				<tr>
					<td><font class="font">Organization:</font></td>
					<td><input name="organization" value="chezfrancois.jfire.org"></td>
				</tr>				
				
				<tr>
					<td></td>
					<td align="right"><input type="submit" value="Login"></td>
				</tr>
			</table>
		</form>
	    </table>
	    </center>
	</body>
</html>