package com.example.myapplication.data

import com.example.myapplication.model.FirstAidGuide

object FirstAidRepository {

    fun getGuides(): List<FirstAidGuide> {
        return listOf(
            FirstAidGuide(
                title = "CPR (Cardiopulmonary Resuscitation)",
                category = "CARDIAC EMERGENCY",
                symptoms = "Unresponsive person, no breathing or only gasping.",
                emergencySteps = listOf(
                    "1. Call 112 / 108 immediately or ask someone nearby to call.",
                    "2. Place person on their back on a firm, flat surface.",
                    "3. Place hands in center of chest (lower half of sternum).",
                    "4. Push hard and fast at a rate of 100-120 compressions per minute (to the beat of 'Staying Alive').",
                    "5. Allow full chest recoil between compressions.",
                    "6. Continue compressions until medical help arrives or AED is available."
                ),
                donts = listOf(
                    "Do NOT stop compressions unless medical experts take over.",
                    "Do NOT perform CPR if the person is conscious and breathing normally."
                )
            ),
            FirstAidGuide(
                title = "Severe Bleeding & Hemorrhage",
                category = "TRAUMA",
                symptoms = "Rapid, continuous blood spurting or flowing heavily from a wound.",
                emergencySteps = listOf(
                    "1. Apply firm, direct pressure on the wound using a clean cloth, sterile gauze, or gloved hand.",
                    "2. Keep pressure continuous for at least 10 minutes without lifting to check.",
                    "3. Elevate the injured area above heart level if no bone fracture is suspected.",
                    "4. If blood soaks through, add another cloth layer on top without removing the original.",
                    "5. Seek immediate hospital care."
                ),
                donts = listOf(
                    "Do NOT remove embedded objects (knives, glass) from wound; stabilize around them.",
                    "Do NOT remove blood-soaked bandages."
                )
            ),
            FirstAidGuide(
                title = "Choking (Heimlich Maneuver)",
                category = "RESPIRATORY",
                symptoms = "Person clutching throat, unable to speak, cough, or breathe.",
                emergencySteps = listOf(
                    "1. Stand behind the choking person and lean them slightly forward.",
                    "2. Wrap arms around their waist.",
                    "3. Make a fist with one hand and place it just above the navel.",
                    "4. Grasp fist with other hand and give quick, upward abdominal thrusts.",
                    "5. Repeat thrusts until object is dislodged or person loses consciousness.",
                    "6. If person becomes unconscious, start CPR."
                ),
                donts = listOf(
                    "Do NOT perform Heimlich thrusts on infants under 1 year (use back blows instead).",
                    "Do NOT slap on back if person is upright and coughing forcefully."
                )
            ),
            FirstAidGuide(
                title = "Burns & Scalds",
                category = "THERMAL INJURY",
                symptoms = "Redness, blistering, severe pain, or charred skin.",
                emergencySteps = listOf(
                    "1. Immediately cool the burn under cool running tap water for 10-20 minutes.",
                    "2. Remove jewelry or tight clothing near burn before swelling starts.",
                    "3. Cover burn loosely with clean cling film or a sterile non-stick bandage.",
                    "4. Keep the victim warm and hydrated."
                ),
                donts = listOf(
                    "Do NOT apply ice, ice water, butter, oil, or toothpaste to burns.",
                    "Do NOT pop blisters or remove stuck burnt clothing."
                )
            ),
            FirstAidGuide(
                title = "Heart Attack",
                category = "CARDIAC EMERGENCY",
                symptoms = "Chest tightness/pain, pressure spreading to arm, neck or jaw, shortness of breath, cold sweat.",
                emergencySteps = listOf(
                    "1. Call 112 / 108 immediately.",
                    "2. Help the person sit down in a comfortable position (W-position: sitting, knees bent).",
                    "3. Loosen tight clothing around neck and waist.",
                    "4. Give Aspirin 300mg to chew if person is not allergic.",
                    "5. Monitor breathing and stay calm until ambulance arrives."
                ),
                donts = listOf(
                    "Do NOT leave the person alone except to call for help.",
                    "Do NOT give anything to eat or drink if person is unconscious."
                )
            ),
            FirstAidGuide(
                title = "Snake Bite & Venomous Stings",
                category = "ENVIRONMENTAL",
                symptoms = "Two puncture marks, severe pain, swelling, numbness, nausea, difficulty breathing.",
                emergencySteps = listOf(
                    "1. Keep victim calm and completely still; movement spreads venom faster.",
                    "2. Remove rings, watches, or tight clothing around bitten limb.",
                    "3. Immobilize the bitten limb using a splint at or below heart level.",
                    "4. Rush to the nearest emergency hospital immediately for anti-venom (AVS)."
                ),
                donts = listOf(
                    "Do NOT cut the bite wound or try to suck out venom.",
                    "Do NOT apply tight tourniquets or ice packs."
                )
            ),
            FirstAidGuide(
                title = "Fractures & Sprains",
                category = "ORTHOPEDIC",
                symptoms = "Severe pain, swelling, bruising, inability to move limb, visible deformity.",
                emergencySteps = listOf(
                    "1. Keep injured area immobilized using a splint or padding.",
                    "2. Apply ice wrapped in cloth for 15-20 minutes to reduce swelling.",
                    "3. Elevate injured limb if possible.",
                    "4. Transport to medical facility for X-ray."
                ),
                donts = listOf(
                    "Do NOT try to realign or force deformed bones back in place.",
                    "Do NOT test if limb can bear weight."
                )
            ),
            FirstAidGuide(
                title = "Stroke (FAST Protocol)",
                category = "NEUROLOGICAL",
                symptoms = "Face drooping, Arm weakness, Speech difficulty, Time to call emergency.",
                emergencySteps = listOf(
                    "1. Perform FAST test: F(Face smile level?), A(Raise both arms?), S(Slurred speech?), T(Time!).",
                    "2. Note exact time symptoms started.",
                    "3. Call 112 / 108 emergency immediately.",
                    "4. Keep person lying on their side (recovery position) if drowsy."
                ),
                donts = listOf(
                    "Do NOT give food, water, or aspirin (could cause choking or worsen bleeding stroke).",
                    "Do NOT let them sleep it off."
                )
            )
        )
    }
}