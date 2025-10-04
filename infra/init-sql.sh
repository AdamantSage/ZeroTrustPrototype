#!/bin/bash
# Example script to initialize database schema
# Run this after SQL Server is up

/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Your_pass@123 -d master -Q "CREATE DATABASE ZeroTrustDB;"
