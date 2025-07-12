package org.FRFood.HTTPHandler;

import org.FRFood.DAO.*;
import io.jsonwebtoken.*;
import org.FRFood.util.*;
import org.FRFood.entity.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.security.SignatureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.FRFood.util.Authenticate.authenticate;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BuyerHandler {
}