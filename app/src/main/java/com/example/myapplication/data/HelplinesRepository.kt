package com.example.myapplication.data

import com.example.myapplication.model.Helpline

object HelplinesRepository {

    fun getIndianHelplines(): List<Helpline> {
        return listOf(
            Helpline(
                title = "National Emergency Number",
                number = "112",
                category = "ALL EMERGENCY",
                description = "Single emergency number for Police, Fire, Ambulance & Rescue across India."
            ),
            Helpline(
                title = "Police Control Room",
                number = "100",
                category = "POLICE",
                description = "Immediate police assistance in case of crime, threats, or public safety issues."
            ),
            Helpline(
                title = "Fire Brigade",
                number = "101",
                category = "FIRE",
                description = "Emergency fire department dispatch for fire hazards and accidents."
            ),
            Helpline(
                title = "Ambulance Services (NHM)",
                number = "102",
                category = "MEDICAL",
                description = "Maternal & Child healthcare ambulance transport (Toll-free)."
            ),
            Helpline(
                title = "Emergency Ambulance (Disaster & Trauma)",
                number = "108",
                category = "MEDICAL",
                description = "24/7 free emergency medical response and trauma care ambulance."
            ),
            Helpline(
                title = "Women Helpline",
                number = "1091",
                category = "WOMEN SAFETY",
                description = "24x7 dedicated emergency assistance & legal support for women."
            ),
            Helpline(
                title = "Childline India",
                number = "1098",
                category = "CHILD CARE",
                description = "Toll-free 24-hour helpline for children in distress or needing care."
            ),
            Helpline(
                title = "Disaster Management (NDRF)",
                number = "1078",
                category = "DISASTER",
                description = "National Disaster Response Force control room for floods, quakes & disasters."
            ),
            Helpline(
                title = "Senior Citizen Helpline (Elderline)",
                number = "14567",
                category = "ELDERLY",
                description = "Information, emotional support, and emergency help for senior citizens."
            ),
            Helpline(
                title = "Mental Health Helpline (KIRAN)",
                number = "1800-599-0019",
                category = "MENTAL HEALTH",
                description = "Free 24/7 mental health & crisis counseling support helpline by Govt of India."
            ),
            Helpline(
                title = "National Cyber Crime Helpline",
                number = "1930",
                category = "CYBER CRIME",
                description = "Immediate reporting of financial cyber frauds and online harassment."
            ),
            Helpline(
                title = "Road Accident Emergency",
                number = "1073",
                category = "ROAD SAFETY",
                description = "Highway accident response and emergency roadside medical help."
            )
        )
    }
}